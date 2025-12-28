// naiko.benchmark.gradle.kts

import naiko.buildlogic.configureAndroid
import naiko.buildlogic.configureTest

plugins {
    id("com.android.test")
    kotlin("android")
}

android {
    configureAndroid(this)
    configureTest()
}
id("naiko.code.lint")
