package app.usefriendly.barely

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class LauncherAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = 0
        }
        activeService = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        if (activeService === this) activeService = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (activeService === this) activeService = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        private var activeService: LauncherAccessibilityService? = null

        fun runGlobalAction(action: Int): Boolean =
            activeService?.performGlobalAction(action) == true
    }
}
