plugins {
    id("naiko.android.library.kmp")
    kotlin("multiplatform")
    alias(kotlinx.plugins.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.naikoDomain)
                api(libs.bundles.db)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.bundles.db.android)
                implementation(projects.naikoSourceApi)
            }
        }
    }
}

android {
    namespace = "naiko.data"
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("naiko.data")
            dialect(libs.sqldelight.dialects.sql)
            schemaOutputDirectory.set(project.file("./src/commonMain/sqldelight"))
        }
    }
}
