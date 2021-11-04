package com.backend

import com.backend.plugins.*
import com.backend.plugins.funksjonsvurdering.funksjonsvurderingRoute
import com.backend.plugins.resources.*
import com.backend.plugins.venterbarn.venterBarnRoute
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.serialization.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

val patientResource = PatientResource("local")
val conditionResource = ConditionResource("local")
val subscriptionResource = SubscriptionResource("local")
val questionnaireResponseResource = QuestionnaireResponseResource("local")
val questionnaireResource = QuestionnaireResource("local")
val taskResource = TaskResource("local")
val binaryResource = BinaryResource("local")
val pdfHandler = PdfHandler()

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

        venterBarnRoute()
        funksjonsvurderingRoute()

        // Create required subscriptions if they do not exist
        subscriptionResource.createDefaultSubscriptions()

        // Create a default patient
        createDefaultPatient()

        // Put QRs already in the server in NAV's inbox
        loadNAVInbox()

        // Create default questionnaires
        questionnaireResource.predefinedQuestionnaires = runBlocking { questionnaireResource.readAll() }
        if (questionnaireResource.predefinedQuestionnaires.isEmpty())
            questionnaireResource.createDefaultQuestionnaires()
    }.start(wait = true)
}

fun createDefaultPatient() {
    // Check if a predetermined patient exists in the fhir server
    val patient = runBlocking { patientResource.search(identifier = "07069012345") }

    // If the patient doesn't exist, create it
    if (patient == null) {
        runBlocking {
            patientResource.create("Kari", "Nordmann", identifierValue = "07069012345",  birthdate = "7-Jun-1990")
            patientResource.create("Ola", "Nordmann", identifierValue = "07069012346",  birthdate = "7-Jun-1991")
        }
    }
}

fun loadNAVInbox() {
    val questionnaireResponses = runBlocking { questionnaireResponseResource.readAll() }
    questionnaireResponses.forEach {
        questionnaireResponseResource.addToInbox(questionnaireResponseResource.jsonParser.encodeResourceToString(it))
    }
}
