// Naiko/buildSrc/build.gradle.kts

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    // Explicitly define versions without relying on version catalogs in buildSrc
    implementation("com.android.tools.build:gradle:8.10.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.21")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.2")
    implementation(gradleApi())
}