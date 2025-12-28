plugins {
    id("naiko.android.library")
    id("naiko.android.library.compose")
    kotlin("android")
}

android {
    namespace = "tachiyomi.presentation.widget"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(projects.aniyomiCoreCommon)
    implementation(projects.aniyomiDomain)
    implementation(projects.aniyomiPresentationCore)
    api(projects.aniyomiI18n)
    api(projects.aniyomiI18nAniyomi)

    implementation(compose.glance)
    implementation(libs.material)

    implementation(kotlinx.immutables)

    implementation(platform(libs.coil.bom))
    implementation(libs.coil.core)

    api(libs.injekt)
}
