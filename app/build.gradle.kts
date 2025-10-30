plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ktlint.gradle)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    id("org.owasp.dependencycheck") version "12.1.8"
}

val newsApiBaseUrl = "https://newsapi.org/v2/"
val newsApiKey = project.findProperty("NEWS_API_KEY") as? String ?: "9594da9dd0c44e55979fdf756d90cddc"
val adminDemoEmail = project.findProperty("ADMIN_DEMO_EMAIL") as? String ?: "admin@test.com"
val adminDemoPassword = project.findProperty("ADMIN_DEMO_PASSWORD") as? String ?: ""

android {
    namespace = "com.example.sgfuturenursingapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sg_future_nursing_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "NEWS_API_BASE_URL", "\"$newsApiBaseUrl\"")
        buildConfigField("String", "NEWS_API_KEY", "\"$newsApiKey\"")
        buildConfigField("String", "ADMIN_DEMO_EMAIL", "\"$adminDemoEmail\"")
        buildConfigField("String", "ADMIN_DEMO_PASSWORD", "\"$adminDemoPassword\"")
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    coreLibraryDesugaring(libs.desugarJdkLibs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended-android:1.6.7")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.gson)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    kapt(libs.hilt.compiler)
    kapt(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

dependencyCheck {
    failBuildOnCVSS = 7.0F
    val resolvedNvdApiKey = project.findProperty("NVD_API_KEY") as? String
        ?: System.getenv("NVD_API_KEY")
    nvd.apply {
        if (!resolvedNvdApiKey.isNullOrBlank()) {
            apiKey = resolvedNvdApiKey
        }
    }
}

ktlint {
    android.set(false)
    filter {
        exclude("**/build/**")
        include("**/src/**/*.kt")
    }
}
