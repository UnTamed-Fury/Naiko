// naiko.android.library.gradle.kts

import naiko.buildlogic.AndroidConfig
import naiko.buildlogic.configureAndroid
import naiko.buildlogic.configureTest

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    configureAndroid(this)
    configureTest()
}
id("naiko.code.lint")
