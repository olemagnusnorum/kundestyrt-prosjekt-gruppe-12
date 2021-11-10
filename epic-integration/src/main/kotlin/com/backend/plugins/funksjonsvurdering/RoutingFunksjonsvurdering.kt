package com.backend.plugins.funksjonsvurdering

import com.backend.patientResource
import com.backend.pdfHandler
import com.backend.questionnaireResponseResource
import com.backend.taskResource
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.netty.handler.codec.http.HttpResponseStatus

var lastPatient: String = "5"

fun Application.funksjonsvurderingRoute() {
    // Setup nav and doctor routing
    navRoute()
    doctorRoute()

    routing {
        get("/") {
            call.respondTemplate("index.ftl")
        }

        // Landing page - navigation
        get("/funksjonsvurdering") {
            call.respondTemplate("funksjonsvurdering/index.ftl")
        }

        // Fhir subscription endpoint for questionnaireResponse subscription
        put("funksjonsvurdering/questionnaireResponse-subscription/{...}"){
            val body = call.receive<String>()
            println("message received")
            questionnaireResponseResource.addToInbox(body)

            val questionnaireResponse = questionnaireResponseResource.parse(body)
            val patient = patientResource.read(questionnaireResponse.subject.reference.substringAfter("/"))

            val header = "NAV respons fra Helseplattformen\n" +
                    "Dato: ${questionnaireResponse.meta.lastUpdated}\n" +
                    "Pasient: ${patient.name[0].given[0]} ${patient.name[0].family}"

            pdfHandler.writeToPdf(header, questionnaireResponseResource.jsonParser.setPrettyPrint(true).encodeResourceToString(questionnaireResponse), "${patient.identifier[0].value}.pdf")
            call.respond(HttpResponseStatus.CREATED)
        }

        // Fhir subscription endpoint for task subscription
        put("funksjonsvurdering/task-subscription/{...}"){
            val body = call.receive<String>()
            println("message received")
            taskResource.addToInbox(body)
            call.respond(HttpResponseStatus.CREATED)
        }
    }
}
