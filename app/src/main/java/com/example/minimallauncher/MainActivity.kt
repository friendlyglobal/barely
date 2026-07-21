package com.example.minimallauncher

import android.app.role.RoleManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: LauncherRepository
    private lateinit var widgetController: WidgetHostController
    private lateinit var roleRequest: ActivityResultLauncher<Intent>
    private lateinit var widgetPicker: ActivityResultLauncher<Intent>
    private lateinit var widgetConfigurator: ActivityResultLauncher<Intent>
    private var refreshJob: Job? = null
    private var windowLayoutJob: Job? = null

    private var snapshot by androidx.compose.runtime.mutableStateOf(LauncherSnapshot())
    private var favoriteKeys by androidx.compose.runtime.mutableStateOf(emptySet<String>())
    private var isHomeRoleHeld by androidx.compose.runtime.mutableStateOf(false)
    private var isLoading by androidx.compose.runtime.mutableStateOf(true)
    private var showGestureCoach by androidx.compose.runtime.mutableStateOf(false)
    private var homeRequestId by androidx.compose.runtime.mutableIntStateOf(0)
    private var widgetIds by androidx.compose.runtime.mutableStateOf(emptyList<Int>())
    private var pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var foldingFeature by androidx.compose.runtime.mutableStateOf<FoldingFeature?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        roleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            updateRoleState()
            refresh()
        }
        widgetPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val widgetId = result.data?.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                pendingWidgetId,
            ) ?: pendingWidgetId
            if (result.resultCode == RESULT_OK && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                configureOrAddWidget(widgetId)
            } else {
                widgetController.discardWidgetId(widgetId)
                pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            }
        }
        widgetConfigurator = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val widgetId = pendingWidgetId
            if (result.resultCode == RESULT_OK && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                widgetIds = widgetController.addWidget(widgetId)
            } else {
                widgetController.discardWidgetId(widgetId)
            }
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        }
        repository = LauncherRepository(this) { refresh() }
        widgetController = WidgetHostController(this)
        favoriteKeys = repository.favoriteKeys()
        widgetIds = widgetController.savedWidgetIds()
        pendingWidgetId = savedInstanceState?.getInt(
            PENDING_WIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        showGestureCoach = !repository.isGestureCoachSeen()

        setContent {
            MinimalLauncherTheme {
                LauncherScreen(
                    snapshot = snapshot,
                    favoriteKeys = favoriteKeys,
                    isHomeRoleHeld = isHomeRoleHeld,
                    isLoading = isLoading,
                    showGestureCoach = showGestureCoach,
                    homeRequestId = homeRequestId,
                    widgetIds = widgetIds,
                    widgetHost = widgetController.host,
                    widgetManager = widgetController.manager,
                    foldingFeature = foldingFeature,
                    onRequestHomeRole = ::requestHomeRole,
                    onGestureCoachSeen = {
                        repository.markGestureCoachSeen()
                        showGestureCoach = false
                    },
                    onLaunchApp = { app -> perform(getString(R.string.error_open_app, app.label)) {
                        repository.launch(app)
                    } },
                    onLaunchShortcut = { shortcut -> perform(getString(R.string.error_shortcut_unavailable)) {
                        repository.launch(shortcut)
                    } },
                    onToggleFavorite = { app ->
                        favoriteKeys = repository.toggleFavorite(app)
                    },
                    onAppInfo = { app -> perform(getString(R.string.error_open_app_info)) {
                        repository.showAppInfo(app)
                    } },
                    onUninstall = ::requestUninstall,
                    onAddWidget = ::pickWidget,
                    onRemoveWidget = { widgetId ->
                        widgetIds = widgetController.removeWidget(widgetId)
                    },
                    onLockScreen = {
                        runAccessibilityAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                    },
                    onOpenNotifications = {
                        runAccessibilityAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homeRequestId++
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) {
            updateRoleState()
            refresh()
        }
    }

    override fun onStart() {
        super.onStart()
        if (::widgetController.isInitialized) runCatching { widgetController.startListening() }
        windowLayoutJob = activityScope.launch {
            WindowInfoTracker.getOrCreate(this@MainActivity)
                .windowLayoutInfo(this@MainActivity)
                .collect { layoutInfo ->
                    foldingFeature = layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()
                }
        }
    }

    override fun onStop() {
        windowLayoutJob?.cancel()
        windowLayoutJob = null
        if (::widgetController.isInitialized) runCatching { widgetController.stopListening() }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(PENDING_WIDGET_ID, pendingWidgetId)
        super.onSaveInstanceState(outState)
    }

    private fun updateRoleState() {
        val roleManager = getSystemService(RoleManager::class.java)
        isHomeRoleHeld = roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
            roleManager.isRoleHeld(RoleManager.ROLE_HOME)
    }

    private fun requestHomeRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        ) {
            roleRequest.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
        }
    }

    private fun requestUninstall(app: LauncherApp) {
        perform(getString(R.string.error_start_uninstall)) {
            startActivity(Intent(Intent.ACTION_DELETE).apply {
                data = "package:${app.packageName}".toUri()
                putExtra(Intent.EXTRA_RETURN_RESULT, false)
            })
        }
    }

    private fun pickWidget() {
        val widgetId = widgetController.allocateWidgetId()
        pendingWidgetId = widgetId
        runCatching {
            widgetPicker.launch(
                Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    widgetId,
                ),
            )
        }.onFailure {
            widgetController.discardWidgetId(widgetId)
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            Toast.makeText(this, R.string.error_add_widget, Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureOrAddWidget(widgetId: Int) {
        val info = widgetController.manager.getAppWidgetInfo(widgetId)
        if (info == null) {
            widgetController.discardWidgetId(widgetId)
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            Toast.makeText(this, R.string.error_add_widget, Toast.LENGTH_SHORT).show()
            return
        }
        val configurationIsOptional = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            info.widgetFeatures
                .and(AppWidgetProviderInfo.WIDGET_FEATURE_CONFIGURATION_OPTIONAL) != 0
        if (info.configure == null || configurationIsOptional) {
            widgetIds = widgetController.addWidget(widgetId)
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            return
        }

        pendingWidgetId = widgetId
        runCatching {
            widgetConfigurator.launch(
                Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = info.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                },
            )
        }.onFailure {
            widgetController.discardWidgetId(widgetId)
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            Toast.makeText(this, R.string.error_add_widget, Toast.LENGTH_SHORT).show()
        }
    }

    private fun runAccessibilityAction(action: Int) {
        if (LauncherAccessibilityService.runGlobalAction(action)) return
        Toast.makeText(
            this,
            R.string.enable_accessibility_gestures,
            Toast.LENGTH_LONG,
        ).show()
        runCatching {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun refresh() {
        refreshJob?.cancel()
        refreshJob = activityScope.launch {
            isLoading = true
            try {
                snapshot = repository.load()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.error_refresh_apps),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            isLoading = false
        }
    }

    private inline fun perform(errorMessage: String, action: () -> Unit) {
        runCatching(action).onFailure {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        refreshJob?.cancel()
        if (::repository.isInitialized) repository.close()
        activityScope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val PENDING_WIDGET_ID = "pending_widget_id"
    }
}
