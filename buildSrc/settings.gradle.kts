enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "naiko-buildSrc"

val catalogDir = rootDir.parentFile.resolve("gradle")
println("BuildSrc Settings: Catalog Dir: $catalogDir")
println("BuildSrc Settings: Libs exists: " + catalogDir.resolve("libs.versions.toml").exists())

dependencyResolutionManagement {
    versionCatalogs {
        create("buildLibs") {
            from(files(catalogDir.resolve("libs.versions.toml")))
        }
        create("buildAndroidx") {
            from(files(catalogDir.resolve("androidx.versions.toml")))
        }
        create("buildCompose") {
            from(files(catalogDir.resolve("compose.versions.toml")))
        }
        create("buildKotlinx") {
            from(files(catalogDir.resolve("kotlinx.versions.toml")))
        }
    }
}