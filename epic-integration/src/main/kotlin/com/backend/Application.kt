package com.backend

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
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
import org.hl7.fhir.instance.model.api.IBaseResource

val subscriptionCommunication = SubscriptionCommunication("local")
val questionnaireResponseCommunication = QuestionnaireResponseCommunication("local")

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
        funksjonsvurderingRoute(questionnaireResponseCommunication)

        // Create required subscriptions if they do not exist
        subscriptionCommunication.createDefaultSubscriptions()

        // Create a default patient
        createDefaultPatient()

        // Put QRs already in the server in NAV's inbox
        loadNAVInbox()
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

fun loadNAVInbox() {
    val ctx: FhirContext = FhirContext.forR4()
    val jsonParser: IParser = ctx.newJsonParser()
    val bundle = runBlocking { questionnaireResponseCommunication.getAllQuestionnaireResponses() }
    val resources = bundle.entry
    for (resource in resources) {
        val final = resource.resource as IBaseResource
        questionnaireResponseCommunication.addToInbox(jsonParser.encodeResourceToString(final))
    }
}
