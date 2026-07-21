plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.minimallauncher"
    compileSdk = 37
    compileSdkMinor = 0

    defaultConfig {
        applicationId = "com.example.minimallauncher"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "0.3"
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    androidResources {
        generateLocaleConfig = true
        localeFilters += listOf(
            "ar",
            "de",
            "en",
            "es",
            "fr",
            "hi",
            "id",
            "it",
            "ja",
            "ko",
            "pt-rBR",
            "ru",
            "zh-rCN",
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.window:window:1.5.1")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3:1.5.0-alpha24")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
}
