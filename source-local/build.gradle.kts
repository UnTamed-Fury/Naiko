import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("naiko.android.library.kmp")
    kotlin("multiplatform")
}

kotlin {
    androidTarget()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.aniyomiSourceApi)
                api(projects.aniyomiI18n)
                api(projects.aniyomiI18nAniyomi)

                implementation(libs.unifile)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(projects.aniyomiCoreArchive)
                implementation(projects.aniyomiCoreCommon)
                implementation(projects.aniyomiCoreMetadata)

                // Move ChapterRecognition to separate module?
                implementation(projects.aniyomiDomain)

                implementation(kotlinx.bundles.serialization)
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}

android {
    namespace = "tachiyomi.source.local"
}