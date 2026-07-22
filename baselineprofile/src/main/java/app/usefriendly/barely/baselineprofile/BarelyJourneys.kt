package app.usefriendly.barely.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "app.usefriendly.barely"
private const val WAIT_TIMEOUT_MS = 5_000L

internal fun MacrobenchmarkScope.startClassicHome() {
    startActivityAndWait()
    val device = device
    device.findObject(By.text("Classic"))?.click()
    device.findObject(By.text("Continue"))?.click()
    device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE)), WAIT_TIMEOUT_MS)
    device.waitForIdle()
}

internal fun UiDevice.openSearch() {
    val width = displayWidth
    val height = displayHeight
    swipe(width / 2, height * 4 / 5, width / 2, height / 4, 18)
    waitForIdle()
}

internal fun UiDevice.openApps() {
    val width = displayWidth
    val height = displayHeight
    swipe(width * 4 / 5, height / 2, width / 5, height / 2, 18)
    wait(Until.hasObject(By.text("Apps")), WAIT_TIMEOUT_MS)
}

internal fun UiDevice.scrollApps() {
    findObject(By.scrollable(true))?.scroll(Direction.DOWN, 0.75f)
        ?: swipe(displayWidth / 2, displayHeight * 3 / 4, displayWidth / 2, displayHeight / 4, 16)
    waitForIdle()
}
