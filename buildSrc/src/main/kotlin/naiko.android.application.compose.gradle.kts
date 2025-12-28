// naiko.android.application.compose.gradle.kts

import naiko.buildlogic.configureCompose

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    configureCompose(this)
}
