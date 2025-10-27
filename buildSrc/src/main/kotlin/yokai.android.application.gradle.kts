import yokai.build.configureAndroid
import yokai.build.configureTest

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
