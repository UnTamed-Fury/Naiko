// naiko.android.library.compose.gradle.kts

import naiko.buildlogic.configureCompose

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    configureCompose(this)
}
id("naiko.code.lint")
