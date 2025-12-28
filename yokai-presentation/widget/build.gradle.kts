import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("naiko.android.library")
    id("naiko.android.library.compose")
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
    implementation(projects.yokaiCoreMain)
    implementation(projects.yokaiData)
    implementation(projects.yokaiDomain)
    implementation(projects.yokaiI18n)
    implementation(projects.yokaiPresentationCore)
    implementation(projects.yokaiSourceApi)  // Access to SManga

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
