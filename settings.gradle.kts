// Naiko/settings.gradle.kts

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://www.jitpack.io")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/releases/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://www.jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/releases/")
    }
    versionCatalogs {
        create("androidx") {
            from(files("gradle/androidx.versions.toml"))
        }
        create("compose") {
            from(files("gradle/compose.versions.toml"))
        }
        create("kotlinx") {
            from(files("gradle/kotlinx.versions.toml"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Naiko"

// Main Naiko app module
include(":app")

// Naiko modules (originally from the 'yokai' folder)
include(":naiko-core-archive")
include(":naiko-core-main")
include(":naiko-data")
include(":naiko-domain")
include(":naiko-i18n")
include(":naiko-presentation-core")
include(":naiko-presentation-widget") // Renamed from presentation:widget for consistency
include(":naiko-source-api")

// Aniyomi modules (originally from the 'aniyomi' folder)
include(":aniyomi-core-metadata")
include(":aniyomi-core-archive")
include(":aniyomi-core-common")
include(":aniyomi-data")
include(":aniyomi-domain")
include(":aniyomi-i18n")
include(":aniyomi-i18n-aniyomi")
include(":aniyomi-macrobenchmark")
include(":aniyomi-presentation-core")
include(":aniyomi-presentation-widget")
include(":aniyomi-source-api")
include(":aniyomi-source-local")
include(":source-local")