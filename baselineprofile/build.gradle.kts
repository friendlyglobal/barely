plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    namespace = "app.usefriendly.barely.baselineprofile"
    compileSdk = 37
    compileSdkMinor = 0

    defaultConfig {
        minSdk = 29
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.enabledRules"] =
            "BaselineProfile"
    }

    targetProjectPath = ":app"
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation("androidx.test.ext:junit:1.3.0")
    implementation("androidx.test.uiautomator:uiautomator:2.4.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.4.1")
}
