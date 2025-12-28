import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsPlugin
import com.google.gms.googleservices.GoogleServicesPlugin
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("naiko.android.application")
    id("naiko.android.application.compose")
    alias(kotlinx.plugins.serialization)
    alias(kotlinx.plugins.parcelize)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.aboutlibraries.android)
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.services) apply false
    // Add Aniyomi specific plugin for shortcut helper if needed
    // id("com.github.zellius.shortcut-helper") // Check if this is needed and replace with alias if available in libs
}

if (gradle.startParameter.taskRequests.toString().contains("standard", true)) {
    apply<CrashlyticsPlugin>()
    apply<GoogleServicesPlugin>()
}

fun runCommand(command: String): String {
    val result = providers.exec { commandLine(command.split(" ")) }
    return result.standardOutput.asText.get().trim()
}

@Suppress("PropertyName")
val _versionName = "1.0.0" // Start with a new version for Naiko
val betaCount by lazy {
    val betaTags = runCommand("git tag -l --sort=refname v${_versionName}-b*")

    if (betaTags.isNotEmpty()) {
        val betaTag = betaTags.split("\n").last().substringAfter("-b").toIntOrNull()
        ((betaTag ?: 0) + 1)
    } else {
        1
    }.toString()
}
val commitCount by lazy { runCommand("git rev-list --count HEAD") }
val commitHash by lazy { runCommand("git rev-parse --short HEAD") }
val buildTime: String by lazy {
    val df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    LocalDateTime.now(ZoneOffset.UTC).format(df)
}

val supportedAbis = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

android {
    defaultConfig {
        applicationId = "com.fury.naiko" // Changed from eu.kanade.tachiyomi
        versionCode = 1 // Starting new for Naiko
        versionName = _versionName // Starting new for Naiko
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        buildConfigField("String", "COMMIT_COUNT", """${commitCount}""")
        buildConfigField("String", "BETA_COUNT", """${betaCount}""")
        buildConfigField("String", "COMMIT_SHA", """${commitHash}""")
        buildConfigField("String", "BUILD_TIME", """${buildTime}""")
        buildConfigField("Boolean", "INCLUDE_UPDATER", "false")
        buildConfigField("Boolean", "BETA", "false")
        buildConfigField("Boolean", "NIGHTLY", "false")

        ndk {
            // False positive, we have x86 abi support
            //noinspection ChromeOsAbiSupport
            abiFilters += supportedAbis
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            // False positive, we have x86 abi support
            //noinspection ChromeOsAbiSupport
            include(*supportedAbis.toTypedArray())
            isUniversalApk = true
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug" // Simplified suffix
            versionNameSuffix = "-d${commitCount}"
        }
        getByName("release") {
            applicationIdSuffix = ".naiko" // Simplified suffix
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
        create("beta") {
            initWith(getByName("release"))
            buildConfigField("boolean", "BETA", "true")

            matchingFallbacks.add("release")
            versionNameSuffix = "-b${betaCount}"
        }
        create("nightly") {
            initWith(getByName("release"))
            buildConfigField("boolean", "BETA", "true")
            buildConfigField("boolean", "NIGHTLY", "true")

            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
            versionNameSuffix = "-r${commitCount}"
            applicationIdSuffix = ".nightly" // Simplified suffix
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        aidl = false
        renderScript = false
        shaders = false
    }

    flavorDimensions.add("default")

    productFlavors {
        create("standard") {
            buildConfigField("Boolean", "INCLUDE_UPDATER", "true")
            dimension = "default"
        }
        create("dev") {
            resourceConfigurations.clear()
            resourceConfigurations.add("en")
            dimension = "default"
        }
    }

    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
        abortOnError = false
        checkReleaseBuilds = false
    }

    namespace = "com.fury.naiko" // Changed from eu.kanade.tachiyomi
}

dependencies {
    // Yokai modules
    implementation(projects.yokaiCoreArchive)
    implementation(projects.yokaiCoreMain)
    implementation(projects.yokaiData)
    implementation(projects.yokaiDomain)
    implementation(projects.yokaiI18n)
    implementation(projects.yokaiPresentationCore)
    implementation(projects.yokaiPresentationWidget)
    implementation(projects.yokaiSourceApi)

    // Aniyomi modules
    implementation(projects.aniyomiCoreMetadata)
    implementation(projects.aniyomiCoreArchive)
    implementation(projects.aniyomiCoreCommon)
    implementation(projects.aniyomiData)
    implementation(projects.aniyomiDomain)
    implementation(projects.aniyomiI18n)
    implementation(projects.aniyomiI18nAniyomi)
    implementation(projects.aniyomiMacrobenchmark)
    implementation(projects.aniyomiPresentationCore)
    implementation(projects.aniyomiPresentationWidget)
    implementation(projects.aniyomiSourceApi)
    implementation(projects.aniyomiSourceLocal)

    // Compose
    implementation(platform(compose.bom))
    implementation(compose.bundles.compose)
    debugImplementation(compose.ui.tooling)
    implementation(libs.compose.theme.adapter3)
    implementation(compose.webview)

    implementation(libs.flexbox)
    implementation(libs.material)
    implementation(androidx.bundles.androidx)
    implementation(platform(libs.firebase))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.rxandroid)
    implementation(libs.rxjava)
    implementation(libs.rxrelay)
    debugImplementation(libs.chucker.library)
    releaseImplementation(libs.chucker.library.no.op)
    "nightlyImplementation"(libs.chucker.library.no.op)
    "betaImplementation"(libs.chucker.library.no.op)
    implementation(kotlin("reflect", version = kotlinx.versions.kotlin.get()))
    implementation(kotlinx.bundles.serialization)
    implementation(libs.quickjs.android)
    implementation(libs.disklrucache)
    implementation(libs.unifile)
    implementation(libs.libarchive)
    implementation(libs.jsoup)
    implementation(libs.play.services.gcm)
    implementation(libs.sqlite.android)
    implementation(libs.bundles.sqlite)
    implementation(libs.conductor)
    implementation(libs.conductor.support.preference)
    implementation(platform(libs.coil.bom))
    implementation(libs.bundles.coil)
    implementation(libs.subsamplingscaleimageview) {
        exclude(module = "image-decoder")
    }
    implementation(libs.image.decoder)
    implementation(libs.java.nat.sort)
    implementation(libs.aboutlibraries)
    implementation(libs.fastadapter)
    implementation(libs.fastadapter.extensions.binding)
    implementation(libs.flexible.adapter)
    implementation(libs.flexible.adapter.ui)
    implementation(libs.viewstatepageradapter)
    implementation(libs.slice)
    implementation(libs.markwon)
    implementation(libs.photoview)
    implementation(libs.directionalviewpager)
    implementation(libs.viewtooltip)
    implementation(libs.taptargetview)
    implementation(libs.insetter)
    implementation(libs.compose.materialmotion)
    implementation(libs.compose.grid)
    implementation(libs.reorderable)
    implementation(libs.swipe)
    implementation(libs.bundles.voyager)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(platform(kotlinx.coroutines.bom))
    implementation(kotlinx.bundles.coroutines)
    implementation(libs.conscrypt.android)
    implementation(libs.mpandroidchart)
    implementation(kotlinx.immutable)

    // Player dependencies
    implementation(libs.bundles.mpv.player)

    // Logging
    implementation(libs.logcat)
    implementation(libs.kermit)

    // Tests
    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.bundles.test.runtime)
    androidTestImplementation(libs.bundles.test.android)
    testImplementation(kotlinx.coroutines.test)

    debugImplementation(libs.leakcanary.android)
    implementation(libs.leakcanary.plumber)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions.freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-opt-in=coil3.annotation.ExperimentalCoilApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.coroutines.InternalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }

    val copyHebrewStrings = task("copyHebrewStrings", type = Copy::class) {
        from("./src/main/res/values-he")
        into("./src/main/res/values-iw")
        include("**/*")
    }

    preBuild {
        dependsOn(
            copyHebrewStrings
        )
    }
}