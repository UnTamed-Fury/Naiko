import yokai.build.configureAndroid
import yokai.build.configureTest

plugins {
    id("com.android.library")
}

android {
    configureAndroid(this)
    configureTest()
}
