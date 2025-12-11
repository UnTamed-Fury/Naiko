import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("yokai.android.library")
    id("yokai.android.library.compose")
    kotlin("android")
}

android {
    namespace = "yokai.presentation.widget"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.main)
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.i18n)
    implementation(projects.presentation.core)
    implementation(projects.source.api)  // Access to SManga

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
