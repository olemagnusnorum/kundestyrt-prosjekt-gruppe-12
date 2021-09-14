val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.30"

    //this is for searialization plugin
    kotlin("plugin.serialization") version "1.5.30"
}

group = "com.backend"
version = "0.0.1"
application {
    mainClass.set("com.backend.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")

    //self imported plugins
    //this is for searialization with kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0-RC")
    //this is for serialization with kotlin
    implementation("io.ktor:ktor-serialization:$ktor_version")
}