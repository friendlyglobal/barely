package app.usefriendly.barely

import android.Manifest
import android.app.role.RoleManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.ContactsContract
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toDrawable
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
import kotlinx.coroutines.withContext
import java.util.function.Consumer

class MainActivity : ComponentActivity() {
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: LauncherRepository
    private lateinit var widgetController: WidgetHostController
    private lateinit var roleRequest: ActivityResultLauncher<Intent>
    private lateinit var widgetPicker: ActivityResultLauncher<Intent>
    private lateinit var widgetConfigurator: ActivityResultLauncher<Intent>
    private lateinit var contactsPermissionRequest: ActivityResultLauncher<String>
    private lateinit var exportSettingsDocument: ActivityResultLauncher<String>
    private lateinit var importSettingsDocument: ActivityResultLauncher<Array<String>>
    private var refreshJob: Job? = null
    private var windowLayoutJob: Job? = null

    private var snapshot by androidx.compose.runtime.mutableStateOf(LauncherSnapshot())
    private var favoriteKeys by androidx.compose.runtime.mutableStateOf(emptySet<String>())
    private var isHomeRoleHeld by androidx.compose.runtime.mutableStateOf(false)
    private var isLoading by androidx.compose.runtime.mutableStateOf(true)
    private var showOnboarding by androidx.compose.runtime.mutableStateOf(false)
    private var showGestureCoach by androidx.compose.runtime.mutableStateOf(false)
    private var homeRequestId by androidx.compose.runtime.mutableIntStateOf(0)
    private var widgets by androidx.compose.runtime.mutableStateOf(emptyList<WidgetPlacement>())
    private var widgetProviders by androidx.compose.runtime.mutableStateOf(emptyList<AppWidgetProviderInfo>())
    private var recommendedAppKeys by androidx.compose.runtime.mutableStateOf(emptyList<String>())
    private var recentAppSearches by androidx.compose.runtime.mutableStateOf(emptyList<String>())
    private var launcherSearchLearning by androidx.compose.runtime.mutableStateOf(
        emptyList<LauncherSearchLearning>(),
    )
    private var privateSpaceExpanded by androidx.compose.runtime.mutableStateOf(true)
    private var contacts by androidx.compose.runtime.mutableStateOf(emptyList<LauncherContact>())
    private var hasContactsPermission by androidx.compose.runtime.mutableStateOf(false)
    private var hasNotificationAccess by androidx.compose.runtime.mutableStateOf(false)
    private var launcherSettings by androidx.compose.runtime.mutableStateOf(LauncherSettings())
    private var availableAssistants by androidx.compose.runtime.mutableStateOf(
        emptyList<AssistantPreference>(),
    )
    private var hasGestureAccess by androidx.compose.runtime.mutableStateOf(false)
    private var notificationState by androidx.compose.runtime.mutableStateOf(LauncherNotificationState())
    private var pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var foldingFeature by androidx.compose.runtime.mutableStateOf<FoldingFeature?>(null)
    private var crossWindowBlurEnabled by androidx.compose.runtime.mutableStateOf(false)
    private var blurEnabledListener: Consumer<Boolean>? = null
    private var requestedBackdrop = LauncherBackdrop.CLEAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        observeCrossWindowBlur()

        roleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            updateRoleState()
            refresh()
        }
        contactsPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            hasContactsPermission = granted
            if (granted) {
                refreshContacts()
            } else {
                contacts = emptyList()
                Toast.makeText(this, R.string.contacts_permission_denied, Toast.LENGTH_LONG).show()
            }
        }
        exportSettingsDocument = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            if (uri == null) return@registerForActivityResult
            perform(getString(R.string.settings_export_failed)) {
                contentResolver.openOutputStream(uri, "wt")?.use { output ->
                    output.write(repository.exportPortableSettings().toByteArray())
                } ?: error("Unable to open settings destination")
                Toast.makeText(this, R.string.settings_export_complete, Toast.LENGTH_SHORT).show()
            }
        }
        importSettingsDocument = registerForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri == null) return@registerForActivityResult
            perform(getString(R.string.settings_import_failed)) {
                val encoded = contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                    it.readText()
                } ?: error("Unable to read settings document")
                launcherSettings = repository.importPortableSettings(encoded)
                Toast.makeText(this, R.string.settings_import_complete, Toast.LENGTH_SHORT).show()
            }
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
                widgets = widgetController.addWidget(widgetId)
            } else {
                widgetController.discardWidgetId(widgetId)
            }
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        }
        repository = LauncherRepository(this) { refresh() }
        widgetController = WidgetHostController(this)
        favoriteKeys = repository.favoriteKeys()
        widgets = widgetController.savedWidgets()
        widgetProviders = widgetController.availableProviders()
        refreshLocalSuggestions()
        privateSpaceExpanded = repository.isPrivateSpaceExpanded()
        launcherSettings = repository.launcherSettings()
        availableAssistants = installedAssistantPreferences()
        showOnboarding = !repository.isOnboardingComplete()
        hasGestureAccess = isGestureAccessGranted()
        hasNotificationAccess = isNotificationAccessGranted()
        pendingWidgetId = savedInstanceState?.getInt(
            PENDING_WIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        showGestureCoach = !repository.isGestureCoachSeen()

        setContent {
            BarelyTheme {
                if (showOnboarding) {
                    BarelyOnboarding(
                        initialMode = launcherSettings.homeMode,
                        initialAssistant = launcherSettings.preferredAssistant,
                        availableAssistants = availableAssistants,
                        onComplete = { homeMode, assistant ->
                            updateLauncherSettings(
                                launcherSettings.copy(
                                    homeMode = homeMode,
                                    preferredAssistant = assistant,
                                ),
                            )
                            repository.markOnboardingComplete()
                            if (homeMode == LauncherHomeMode.TERMINAL) {
                                repository.markGestureCoachSeen()
                                showGestureCoach = false
                            }
                            showOnboarding = false
                        },
                    )
                } else LauncherScreen(
                    snapshot = snapshot,
                    favoriteKeys = favoriteKeys,
                    isHomeRoleHeld = isHomeRoleHeld,
                    isLoading = isLoading,
                    showGestureCoach = showGestureCoach,
                    homeRequestId = homeRequestId,
                    widgets = widgets,
                    widgetProviders = widgetProviders,
                    widgetHost = widgetController.host,
                    widgetManager = widgetController.manager,
                    foldingFeature = foldingFeature,
                    backdropBlurEnabled = crossWindowBlurEnabled &&
                        launcherSettings.frostedWallpaper,
                    launcherSettings = launcherSettings,
                    availableAssistants = availableAssistants,
                    privateSpaceExpanded = privateSpaceExpanded,
                    contacts = contacts,
                    hasContactsPermission = hasContactsPermission,
                    hasGestureAccess = hasGestureAccess,
                    hasNotificationAccess = hasNotificationAccess,
                    notificationCounts = if (
                        hasNotificationAccess && launcherSettings.notificationDots
                    ) {
                        notificationState.countsByPackage
                    } else {
                        emptyMap()
                    },
                    nowPlaying = if (hasNotificationAccess && launcherSettings.mediaControls) {
                        notificationState.nowPlaying
                    } else {
                        null
                    },
                    onRequestHomeRole = ::requestHomeRole,
                    onGestureCoachSeen = {
                        repository.markGestureCoachSeen()
                        showGestureCoach = false
                    },
                    recommendedAppKeys = recommendedAppKeys,
                    recentAppSearches = recentAppSearches,
                    launcherSearchLearning = launcherSearchLearning,
                    onLaunchApp = { app ->
                        perform(getString(R.string.error_open_app, app.label)) {
                            repository.launch(app)
                            refreshLocalSuggestions()
                        }
                    },
                    onLaunchShortcut = { shortcut -> perform(getString(R.string.error_shortcut_unavailable)) {
                        repository.launch(shortcut)
                    } },
                    onToggleFavorite = { app ->
                        favoriteKeys = repository.toggleFavorite(app)
                    },
                    onToggleShortcutFavorite = { shortcut ->
                        favoriteKeys = repository.toggleFavorite(shortcut)
                    },
                    onAppInfo = { app -> perform(getString(R.string.error_open_app_info)) {
                        repository.showAppInfo(app)
                    } },
                    onUninstall = ::requestUninstall,
                    onAddWidget = ::pickWidget,
                    onRemoveWidget = { widgetId ->
                        widgets = widgetController.removeWidget(widgetId)
                    },
                    onUpdateWidget = { widgetId, widthSpan, heightDp, position ->
                        widgets = widgetController.updateWidget(
                            widgetId = widgetId,
                            widthSpan = widthSpan,
                            heightDp = heightDp,
                            horizontalPosition = position,
                        )
                    },
                    onMoveWidget = { widgetId, direction ->
                        widgets = widgetController.moveWidget(widgetId, direction)
                    },
                    onSetPrivateSpaceExpanded = { expanded ->
                        repository.setPrivateSpaceExpanded(expanded)
                        privateSpaceExpanded = expanded
                    },
                    onSetPrivateSpaceLocked = { profile, locked ->
                        perform(getString(R.string.error_private_space)) {
                            repository.setPrivateSpaceLocked(profile, locked)
                        }
                    },
                    onExecuteCommand = ::executeCommand,
                    onMediaAction = LauncherNotificationService::performMediaAction,
                    onLockScreen = {
                        runAccessibilityAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                    },
                    onOpenNotifications = {
                        runAccessibilityAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                    },
                    onSettingsChanged = ::updateLauncherSettings,
                    onAppSearchCommitted = { query, app ->
                        repository.recordRecentAppSearch(query)
                        repository.recordSearchSelection(
                            query = query,
                            targetKey = app.searchTargetKey,
                            isPrivate = app.isPrivate,
                        )
                        refreshLocalSuggestions()
                    },
                    onShortcutSearchCommitted = { query, shortcut ->
                        repository.recordRecentAppSearch(query)
                        repository.recordSearchSelection(
                            query = query,
                            targetKey = shortcut.searchTargetKey,
                            isPrivate = shortcut.owner.isPrivate,
                        )
                        refreshLocalSuggestions()
                    },
                    onClearLocalHistory = {
                        repository.clearLocalHistory()
                        refreshLocalSuggestions()
                        Toast.makeText(this, R.string.settings_history_cleared, Toast.LENGTH_SHORT).show()
                    },
                    onOpenAccessibilitySettings = ::openAccessibilitySettings,
                    onOpenNotificationAccess = ::openNotificationAccess,
                    onConfigureContacts = ::configureContacts,
                    onExportSettings = ::exportSettings,
                    onImportSettings = ::importSettings,
                    onBackdropChanged = ::applyBackdrop,
                )
            }
        }
        activityScope.launch {
            LauncherNotificationService.state.collect { state ->
                notificationState = state
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
            refreshContacts()
            hasGestureAccess = isGestureAccessGranted()
            hasNotificationAccess = isNotificationAccessGranted()
            availableAssistants = installedAssistantPreferences()
            widgetProviders = widgetController.availableProviders()
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

    private fun installedAssistantPreferences(): List<AssistantPreference> =
        AssistantPreference.entries.filter { preference ->
            val packageName = preference.packageName ?: return@filter false
            packageManager.getLaunchIntentForPackage(packageName) != null
        }

    private fun requestHomeRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        ) {
            roleRequest.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
        }
    }

    private fun exportSettings() {
        exportSettingsDocument.launch("barely-settings.json")
    }

    private fun importSettings() {
        importSettingsDocument.launch(arrayOf("application/json", "text/plain"))
    }

    private fun requestUninstall(app: LauncherApp) {
        perform(getString(R.string.error_start_uninstall)) {
            startActivity(Intent(Intent.ACTION_DELETE).apply {
                data = "package:${app.packageName}".toUri()
                putExtra(Intent.EXTRA_RETURN_RESULT, false)
            })
        }
    }

    private fun pickWidget(provider: AppWidgetProviderInfo) {
        val widgetId = widgetController.allocateWidgetId()
        pendingWidgetId = widgetId
        if (widgetController.bindWidget(widgetId, provider)) {
            configureOrAddWidget(widgetId)
            return
        }
        runCatching {
            widgetPicker.launch(
                Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, provider.profile)
                },
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
            widgets = widgetController.addWidget(widgetId)
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
        openAccessibilitySettings()
    }

    private fun executeCommand(command: LauncherCommand) {
        when (val action = command.action) {
            is LauncherCommandAction.CopyResult -> perform(getString(R.string.error_command)) {
                getSystemService(ClipboardManager::class.java).setPrimaryClip(
                    ClipData.newPlainText(command.title, action.value),
                )
                Toast.makeText(this, R.string.command_copied, Toast.LENGTH_SHORT).show()
            }

            is LauncherCommandAction.OpenSettings -> perform(getString(R.string.error_command)) {
                startActivity(Intent(action.intentAction))
            }

            is LauncherCommandAction.Dial -> perform(getString(R.string.error_command)) {
                startActivity(Intent(Intent.ACTION_DIAL, "tel:${action.phoneNumber}".toUri()))
            }

            is LauncherCommandAction.AskAssistant -> perform(getString(R.string.error_command)) {
                startActivity(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage(action.packageName)
                        putExtra(Intent.EXTRA_TEXT, action.prompt)
                    },
                )
            }

            is LauncherCommandAction.OpenAssistant -> perform(getString(R.string.error_command)) {
                val launchIntent = packageManager.getLaunchIntentForPackage(action.packageName)
                checkNotNull(launchIntent) { "Assistant app is unavailable" }
                startActivity(launchIntent)
            }

            LauncherCommandAction.RequestContactsPermission -> {
                contactsPermissionRequest.launch(Manifest.permission.READ_CONTACTS)
            }

            LauncherCommandAction.OpenNotificationAccess -> perform(
                getString(R.string.error_command),
            ) {
                openNotificationAccess()
            }

            is LauncherCommandAction.ToggleNotificationDots -> {
                updateLauncherSettings(
                    launcherSettings.copy(notificationDots = action.enabled),
                )
            }

            is LauncherCommandAction.ToggleMediaControls -> {
                updateLauncherSettings(
                    launcherSettings.copy(mediaControls = action.enabled),
                )
            }
        }
    }

    private fun isNotificationAccessGranted(): Boolean =
        packageName in NotificationManagerCompat.getEnabledListenerPackages(this)

    private fun isGestureAccessGranted(): Boolean =
        getSystemService(AccessibilityManager::class.java)
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { service ->
                service.resolveInfo.serviceInfo.packageName == packageName &&
                    service.resolveInfo.serviceInfo.name == LauncherAccessibilityService::class.java.name
            }

    private fun updateLauncherSettings(settings: LauncherSettings) {
        repository.setLauncherSettings(settings)
        launcherSettings = settings
        refreshLocalSuggestions()
        applyBackdrop(requestedBackdrop)
    }

    private fun refreshLocalSuggestions() {
        recommendedAppKeys = repository.recommendedAppKeys()
        recentAppSearches = repository.recentAppSearches()
        launcherSearchLearning = repository.launcherSearchLearning()
    }

    private fun openAccessibilitySettings() {
        perform(getString(R.string.error_command)) {
            val serviceComponent = ComponentName(
                this,
                LauncherAccessibilityService::class.java,
            )
            val componentKey = serviceComponent.flattenToString()
            val fragmentArguments = Bundle().apply {
                putString(SETTINGS_FRAGMENT_ARGUMENT_KEY, componentKey)
            }
            fun Intent.withServiceHint(): Intent =
                putExtra(SETTINGS_FRAGMENT_ARGUMENT_KEY, componentKey)
                    .putExtra(SETTINGS_SHOW_FRAGMENT_ARGUMENTS, fragmentArguments)

            val candidates = listOf(
                Intent(ACTION_ACCESSIBILITY_DETAILS_SETTINGS)
                    .putExtra(Intent.EXTRA_COMPONENT_NAME, componentKey)
                    .withServiceHint(),
                Intent(SAMSUNG_ACCESSIBILITY_INSTALLED_SERVICES).withServiceHint(),
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).withServiceHint(),
            )
            val opened = candidates.any { intent ->
                intent.resolveActivity(packageManager) != null &&
                    runCatching { startActivity(intent) }.isSuccess
            }
            check(opened) { "No Accessibility settings activity is available" }
        }
    }

    private fun openNotificationAccess() {
        perform(getString(R.string.error_command)) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun configureContacts() {
        if (!hasContactsPermission) {
            contactsPermissionRequest.launch(Manifest.permission.READ_CONTACTS)
            return
        }
        perform(getString(R.string.error_command)) {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    "package:$packageName".toUri(),
                ),
            )
        }
    }

    private fun observeCrossWindowBlur() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val manager = getSystemService(WindowManager::class.java)
        val listener = Consumer<Boolean> { enabled ->
            crossWindowBlurEnabled = enabled
            applyBackdrop(requestedBackdrop)
        }
        blurEnabledListener = listener
        manager.addCrossWindowBlurEnabledListener(mainExecutor, listener)
    }

    private fun applyBackdrop(backdrop: LauncherBackdrop) {
        requestedBackdrop = backdrop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val shouldBlur = launcherSettings.frostedWallpaper &&
            crossWindowBlurEnabled &&
            backdrop != LauncherBackdrop.CLEAR
        val radiusDp = if (!shouldBlur) {
            0
        } else when (backdrop) {
            LauncherBackdrop.CLEAR -> 0
            LauncherBackdrop.FROSTED -> BarelyVisualTokens.frostedBlurRadiusDp
            LauncherBackdrop.SEARCH -> BarelyVisualTokens.searchBlurRadiusDp
        }
        val attributes = window.attributes
        attributes.setBlurBehindRadius(
            (radiusDp * resources.displayMetrics.density).toInt(),
        )
        window.attributes = attributes
        if (!shouldBlur) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
    }

    private fun refreshContacts() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED
        hasContactsPermission = granted
        if (!granted) {
            contacts = emptyList()
            return
        }
        activityScope.launch {
            contacts = withContext(Dispatchers.IO) {
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                )
                val results = mutableListOf<LauncherContact>()
                contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC",
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow(projection[0])
                    val normalizedNumberIndex = cursor.getColumnIndexOrThrow(projection[1])
                    val numberIndex = cursor.getColumnIndexOrThrow(projection[2])
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameIndex)?.trim().orEmpty()
                        val number = cursor.getString(normalizedNumberIndex)
                            ?: cursor.getString(numberIndex)
                            ?: continue
                        if (name.isNotEmpty()) results += LauncherContact(name, number)
                    }
                }
                results.distinctBy { "${it.name}:${it.phoneNumber}" }
            }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurEnabledListener?.let { listener ->
                getSystemService(WindowManager::class.java)
                    .removeCrossWindowBlurEnabledListener(listener)
            }
        }
        applyBackdrop(LauncherBackdrop.CLEAR)
        if (::repository.isInitialized) repository.close()
        activityScope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val ACTION_ACCESSIBILITY_DETAILS_SETTINGS =
            "android.settings.ACCESSIBILITY_DETAILS_SETTINGS"
        const val SAMSUNG_ACCESSIBILITY_INSTALLED_SERVICES =
            "com.samsung.accessibility.installed_service"
        const val SETTINGS_FRAGMENT_ARGUMENT_KEY = ":settings:fragment_args_key"
        const val SETTINGS_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"
        const val PENDING_WIDGET_ID = "pending_widget_id"
    }
}
