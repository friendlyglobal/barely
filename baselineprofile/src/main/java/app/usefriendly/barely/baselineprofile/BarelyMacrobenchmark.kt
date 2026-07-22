package app.usefriendly.barely.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BarelyMacrobenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun coldStartup() = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require,
        ),
        startupMode = StartupMode.COLD,
        iterations = 10,
        setupBlock = { pressHome() },
    ) {
        startClassicHome()
    }

    @Test
    fun warmHomeResume() = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require,
        ),
        startupMode = StartupMode.WARM,
        iterations = 10,
        setupBlock = {
            startClassicHome()
            pressHome()
        },
    ) {
        startActivityAndWait()
    }

    @Test
    fun openSearchFrames() = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require,
        ),
        iterations = 10,
        setupBlock = {
            killProcess()
            startClassicHome()
        },
    ) {
        device.openSearch()
    }

    @Test
    fun firstAppsScrollFrames() = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require,
        ),
        iterations = 10,
        setupBlock = {
            killProcess()
            startClassicHome()
            device.openApps()
        },
    ) {
        device.scrollApps()
    }
}
