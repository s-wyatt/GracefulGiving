plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.gracechurch.gracefulgiving"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gracechurch.gracefulgiving"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }
    }
    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}
ksp {
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}
dependencies {
    // AndroidX basics
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)

    // ML Kit Text Recognition - GENTLE FIX: Now using version catalog
    implementation(libs.mlkit.text.recognition)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Compose BOM - Best Practice: Declare this first among Compose dependencies
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Material Icons
    implementation(libs.material.icons.extended)

    // CameraX - GENTLE FIX: Now using version catalog
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // JUnit
    testImplementation("junit:junit:4.13.2")

    implementation(libs.accompanist.permissions)
}
