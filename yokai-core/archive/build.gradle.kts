plugins {
    id("yokai.android.library")
    kotlin("android")
    alias(kotlinx.plugins.serialization)
}

android {
    namespace = "yokai.core.archive"
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.libarchive)
    implementation(libs.unifile)
}
