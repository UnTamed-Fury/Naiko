// naiko.android.application.gradle.kts

import naiko.buildlogic.AndroidConfig
import naiko.buildlogic.configureAndroid
import naiko.buildlogic.configureTest

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    defaultConfig {
        targetSdk = AndroidConfig.TARGET_SDK
    }
    configureAndroid(this)
    configureTest()
}
id("naiko.code.lint")
