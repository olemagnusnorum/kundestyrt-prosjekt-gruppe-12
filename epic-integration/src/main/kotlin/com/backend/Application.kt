package com.backend

import com.backend.plugins.*

import io.ktor.server.engine.*
import io.ktor.server.netty.*

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.serialization.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json


fun main() {
    val epicCommunication = EpicCommunication()
    //runBlocking { println(epicCommunication.patientSearch("Kari", "Nordmann", "2013-06-07")) }
    //runBlocking { println(epicCommunication.getCondition()) }

    //Create Condition
    val response = runBlocking { epicCommunication.createCondition(
        "erXuFYUfucBZaryVksYEcMg3", "My second best note",
        "2015-01-01", "2015-08-30") }

    //Get the previously created Condition from id
    runBlocking { println(epicCommunication.getCondition(response.headers["Location"])) }


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
