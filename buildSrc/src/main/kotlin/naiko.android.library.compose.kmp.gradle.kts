// naiko.android.library.compose.kmp.gradle.kts

import naiko.buildlogic.configureCompose

plugins {
    id("com.android.library")
}

android {
    configureCompose(this)
}
