// naiko.code.lint.gradle.kts

plugins {
    id("org.jmailen.kotlinter")
}

kotlinter {
    ignoreFailures = true
    reporters = arrayOf("checkstyle", "plain")
}
