package com.backend

import com.backend.plugins.*

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.backend.plugins.*
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", watchPaths = listOf("classes", "resources")) {

        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }

        personRoute()
    }.start(wait = true)
}
