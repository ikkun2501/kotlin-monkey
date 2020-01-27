import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // logger
    implementation("org.slf4j:slf4j-api:1.7.13")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    // test
    val junitVersion = "5.6.0"
    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks {
    "test"(Test::class) {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }
}