import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("naiko.android.library")
    id("naiko.android.library.compose")
}

android {
    namespace = "naiko.presentation.widget"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.naikoCoreMain)
    implementation(projects.naikoData)
    implementation(projects.naikoDomain)
    implementation(projects.naikoI18n)
    implementation(projects.naikoPresentationCore)
    implementation(projects.naikoSourceApi)  // Access to SManga

    implementation(androidx.glance.appwidget)

    implementation(platform(libs.coil3.bom))
    implementation(libs.coil3)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions.freeCompilerArgs.addAll(
            "-opt-in=coil3.annotation.ExperimentalCoilApi",
        )
    }
}
