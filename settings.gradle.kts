rootProject.name = "kotlin-monkey"

pluginManagement {
    resolutionStrategy {
        val kotlinVersion: String by settings
        val ktlintVersion: String by settings
        eachPlugin {
            when (requested.id.id) {
                // kotlin
                "org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin.kapt" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
                // ktlint
                "org.jlleitschuh.gradle.ktlint" -> useModule("org.jlleitschuh.gradle:ktlint-gradle:$ktlintVersion")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }
}