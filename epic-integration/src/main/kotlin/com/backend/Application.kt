package com.backend

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import com.backend.plugins.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.serialization.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Questionnaire

val subscriptionResource = SubscriptionResource("local")
val questionnaireResponseResource = QuestionnaireResponseResource("local")
val questionnaireResource = QuestionnaireResource("local")

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

        venterBarnRoute(questionnaireResource)
        funksjonsvurderingRoute(questionnaireResponseResource, questionnaireResource)

        // Create required subscriptions if they do not exist
        subscriptionResource.createDefaultSubscriptions()

        // Create a default patient
        createDefaultPatient()

        // Put QRs already in the server in NAV's inbox
        loadNAVInbox()

        // Create default questionnaires
        val questionnaireBundle = runBlocking { questionnaireResource.searchQuestionnaires() }

        if (questionnaireBundle.entry.size >= 3) {
            for (bundleComponent in questionnaireBundle.entry) {
                val questionnaire = bundleComponent.resource as Questionnaire
                questionnaireResource.predefinedQuestionnaires.add(questionnaire)
            }
        }
        else {
            questionnaireResource.createDefaultQuestionnaires()
        }
    }.start(wait = true)
}

fun createDefaultPatient() {
    // Check if a predetermined patient exists in the fhir server
    val patient = runBlocking {
        val response = patientResource.search(identifier = "07069012345")
        patientResource.parseBundleXMLToPatient(response, isXML = false)
    }

    // If the patient doesn't exist, create it
    if (patient == null) {
        runBlocking {
            patientResource.createPatient("Kari", "Nordmann", identifierValue = "07069012345",  birthdate = "7-Jun-1990")
            patientResource.createPatient("Ola", "Nordmann", identifierValue = "07069012346",  birthdate = "7-Jun-1991")
        }
    }
}

fun loadNAVInbox() {
    val ctx: FhirContext = FhirContext.forR4()
    val jsonParser: IParser = ctx.newJsonParser()
    val bundle = runBlocking { questionnaireResponseResource.getAllQuestionnaireResponses() }
    val resources = bundle.entry
    for (resource in resources) {
        val final = resource.resource as IBaseResource
        questionnaireResponseResource.addToInbox(jsonParser.encodeResourceToString(final))
    }
}
