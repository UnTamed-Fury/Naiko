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
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Naiko"

// Main Naiko app module
include(":app")

// Yokai modules (originally from the 'yokai' folder)
include(":yokai-core-archive")
include(":yokai-core-main")
include(":yokai-data")
include(":yokai-domain")
include(":yokai-i18n")
include(":yokai-presentation-core")
include(":yokai-presentation-widget") // Renamed from presentation:widget for consistency
include(":yokai-source-api")

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