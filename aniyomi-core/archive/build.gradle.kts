plugins {
    id("naiko.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
}

android {
    namespace = "naiko.core.archive"
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.libarchive)
    implementation(libs.unifile)
}
