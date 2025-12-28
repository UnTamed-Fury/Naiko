// naiko.android.library.kmp.gradle.kts

import naiko.buildlogic.AndroidConfig
import naiko.buildlogic.configureAndroid
import naiko.buildlogic.configureTest

plugins {
    id("com.android.library")
}

android {
    configureAndroid(this)
    configureTest()
}
