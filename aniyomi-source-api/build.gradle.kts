import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("naiko.android.library.kmp")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlinx.serialization.json)
                api(libs.koin.injekt)
                api(libs.rxjava)
                api(libs.jsoup)

                implementation(project.dependencies.platform(compose.bom))
                implementation(compose.runtime)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(projects.aniyomiCoreCommon)
                api(libs.preferencektx)

                // Workaround for https://youtrack.jetbrains.com/issue/KT-57605
                implementation(kotlinx.coroutines.android)
                implementation(project.dependencies.platform(kotlinx.coroutines.bom))
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "eu.kanade.tachiyomi.source"

    defaultConfig {
        consumerProguardFile("consumer-proguard.pro")
    }
}
