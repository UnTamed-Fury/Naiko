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

// Naiko modules
include(":naiko-core-archive")
include(":naiko-core-main")
include(":naiko-data")
include(":naiko-domain")
include(":naiko-i18n")
include(":naiko-presentation-core")
include(":naiko-presentation-widget")
include(":naiko-source-api")

project(":naiko-core-archive").projectDir = file("naiko-core/archive")
project(":naiko-core-main").projectDir = file("naiko-core/main")
project(":naiko-presentation-core").projectDir = file("naiko-presentation/core")
project(":naiko-presentation-widget").projectDir = file("naiko-presentation/widget")
project(":naiko-source-api").projectDir = file("naiko-source/api")

// Aniyomi modules
include(":aniyomi-core-metadata")
include(":aniyomi-core-archive")
include(":aniyomi-core-common")
include(":aniyomi-data")
include(":aniyomi-domain")
include(":aniyomi-i18n")
include(":aniyomi-i18n-aniyomi")
include(":aniyomi-presentation-core")
include(":aniyomi-presentation-widget")
include(":aniyomi-source-api")
include(":aniyomi-source-local")

project(":aniyomi-core-archive").projectDir = file("aniyomi-core/archive")
project(":aniyomi-core-common").projectDir = file("aniyomi-core/common")
project(":aniyomi-source-local").projectDir = file("source-local")
