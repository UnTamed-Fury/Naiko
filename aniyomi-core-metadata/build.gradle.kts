plugins {
    id("naiko.android.library")
    kotlin("plugin.serialization")
}

android {
    namespace = "tachiyomi.core.metadata"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(projects.aniyomiSourceApi)

    implementation(kotlinx.bundles.serialization)
}
