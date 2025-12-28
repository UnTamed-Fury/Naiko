plugins {
    id("naiko.android.library")
    kotlin("android")
    alias(kotlinx.plugins.serialization)
}

android {
    namespace = "naiko.core.archive"
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.libarchive)
    implementation(libs.unifile)
}
