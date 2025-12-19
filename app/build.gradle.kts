plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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

        // REMOVE the ksp block from here - it doesn't belong in defaultConfig
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
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

// KSP configuration - KEEP THIS OUTSIDE the android block
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

dependencies {
    // AndroidX basics
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)

    // ML Kit Text Recognition
    implementation(libs.mlkit.text.recognition)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Compose BOM
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
    implementation(libs.sqlcipher)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Material Icons
    implementation(libs.material.icons.extended)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // Testing
    testImplementation(libs.junit)

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Coil
    implementation(libs.coil.compose)

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // uCrop
    implementation("com.github.yalantis:ucrop:2.2.8")
}