plugins {
    // GENTLE FIX: Use only the alias from the version catalog for all plugins.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false // <-- This is the only Hilt plugin declaration needed.
}