package naiko.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

private fun Project.getLibraryCatalog(name: String): VersionCatalog {
    return extensions.getByType<VersionCatalogsExtension>().named(name)
}

internal fun Project.configureAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    val libsCatalog = getLibraryCatalog("libs")
    commonExtension.apply {
        compileSdk = AndroidConfig.COMPILE_SDK
        buildToolsVersion = AndroidConfig.BUILD_TOOLS

        defaultConfig {
            minSdk = AndroidConfig.MIN_SDK
            ndk {
                version = AndroidConfig.NDK
            }
        }

        compileOptions {
            sourceCompatibility = AndroidConfig.JavaVersion
            targetCompatibility = AndroidConfig.JavaVersion
            isCoreLibraryDesugaringEnabled = true
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(AndroidConfig.JvmTarget)
            freeCompilerArgs.addAll(
                "-Xcontext-receivers",
                "-opt-in=kotlin.RequiresOptIn",
            )

            val warningsAsErrors: String? by project
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())

        }
    }

    dependencies {
        "coreLibraryDesugaring"(libsCatalog.findLibrary("desugar").get())
    }
}

internal fun Project.configureCompose(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    val composeCatalog = getLibraryCatalog("compose")
    // This plugin ID is now directly referenced in the main app's build.gradle.kts
    // pluginManager.apply(kotlinx.plugins.compose.compiler.get().pluginId)

    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {
            "implementation"(platform(composeCatalog.findLibrary("bom").get()))
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        featureFlags.set(setOf(ComposeFeatureFlag.OptimizeNonSkippingGroups))

        val enableMetrics = project.providers.gradleProperty("enableComposeCompilerMetrics").orNull.toBoolean()
        val enableReports = project.providers.gradleProperty("enableComposeCompilerReports").orNull.toBoolean()

        val rootBuildDir = rootProject.layout.buildDirectory.asFile.get()
        val relativePath = projectDir.relativeTo(rootDir)

        if (enableMetrics) {
            rootBuildDir.resolve("compose-metrics").resolve(relativePath).let(metricsDestination::set)
        }

        if (enableReports) {
            rootBuildDir.resolve("compose-reports").resolve(relativePath).let(reportsDestination::set)
        }
    }
}

internal fun Project.configureTest() {
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}