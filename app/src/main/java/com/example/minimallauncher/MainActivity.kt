package com.example.minimallauncher

import android.app.role.RoleManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
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
    private lateinit var roleRequest: ActivityResultLauncher<Intent>
    private var refreshJob: Job? = null

    private var snapshot by androidx.compose.runtime.mutableStateOf(LauncherSnapshot())
    private var favoriteKeys by androidx.compose.runtime.mutableStateOf(emptySet<String>())
    private var isHomeRoleHeld by androidx.compose.runtime.mutableStateOf(false)
    private var isLoading by androidx.compose.runtime.mutableStateOf(true)
    private var showGestureCoach by androidx.compose.runtime.mutableStateOf(false)
    private var homeRequestId by androidx.compose.runtime.mutableIntStateOf(0)

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
        repository = LauncherRepository(this) { refresh() }
        favoriteKeys = repository.favoriteKeys()
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
}
