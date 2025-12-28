import naiko.buildlogic.generatedBuildDir
import naiko.buildlogic.getLocalesConfigTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("naiko.android.library.kmp")
    kotlin("multiplatform")
    alias(libs.plugins.moko)
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
            }
        }
        androidMain {
        }
        iosMain {
        }
    }
}

val generatedAndroidResourceDir = generatedBuildDir.resolve("android/res")

android {
    namespace = "naiko.i18n"

    sourceSets {
        val main by getting
        main.res.srcDirs(
            "src/commonMain/resources",
            generatedAndroidResourceDir,
        )
    }
}

multiplatformResources {
    resourcesPackage.set("naiko.i18n")
}

tasks {
   val localesConfigTask = project.getLocalesConfigTask(generatedAndroidResourceDir)
   preBuild {
       dependsOn(localesConfigTask)
   }
}
