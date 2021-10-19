package com.backend

import com.backend.plugins.*

import io.ktor.server.engine.*
import io.ktor.server.netty.*

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.serialization.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

val subscriptionCommunication = SubscriptionCommunication("local")

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
        venterBarnRoute()
        funksjonsvurderingRoute()

        // Create required subscriptions if they do not exist
        // Commented out because the switch to local HAPI servers has not been made yet
        // subscriptionCommunication.createDefaultSubscriptions()

        // Create a default patient
        createDefaultPatient()
    }.start(wait = true)
}

fun createDefaultPatient() {
    // Check if a predetermined patient exists in the fhir server
    val patient = runBlocking {
        val response = patientCommunication.patientSearch(identifier = "07069012345")
        patientCommunication.parseBundleXMLToPatient(response, isXML = false)
    }

    // If the patient doesn't exist, create it
    if (patient == null) {
        runBlocking {
            patientCommunication.createPatient("Kari", "Nordmann", identifierValue = "07069012345",  birthdate = "7-Jun-1990")
            patientCommunication.createPatient("Ola", "Nordmann", identifierValue = "07069012346",  birthdate = "7-Jun-1991")
        }
    }
}

