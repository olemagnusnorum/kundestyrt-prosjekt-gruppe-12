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
    // If the project is run from IntelliJ IDEA, pass the flag '-Dio.ktor.development=true' to VM options to enable development mode.
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
    mainClass.set("com.backend.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    //maven(url = "https://jitpack.io")
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
    // FreeMarker for easy frontend templating
    implementation("io.ktor:ktor-freemarker:$ktor_version")

    //jwtk
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2")

    //http client
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")

    //HAPI
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:5.4.0")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.4.0")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.4.0")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-client:5.4.0")

    //PDFBOX for generating pdfs
    implementation("org.apache.pdfbox:pdfbox:2.0.4")


}