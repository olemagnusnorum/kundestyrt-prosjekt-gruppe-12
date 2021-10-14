package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.runBlocking


fun Application.routingFunksjonsvurdering() {

    routing {

        // Landing page - navigation
        get("/funksjonsvurdering") {
            call.respondTemplate("funksjonsvurdering/index.ftl")
        }

        // Nav landing page
        get("/funksjonsvurdering/nav") {
            call.respondTemplate("funksjonsvurdering/nav.ftl")
        }

        // Nav create questionnaire page
        get("/funksjonsvurdering/create-questionnaire") {
            call.respondTemplate("funksjonsvurdering/create-questionnaire.ftl")
        }

        // Create questionnaire
        post("/funksjonsvurdering/create-questionnaire"){
            val params = call.receiveParameters()

            val question1 = params["question1"]!!
            val question2 = params["question2"]!!
            val question3 = params["question3"]!!

            // Run createQuestionnaire method
            val jsonResponse = runBlocking { epicCommunication.createQuestionnaire(params) }

            // Pass
            val data = mapOf("response" to jsonResponse)
            call.respondTemplate("/funksjonsvurdering/create-questionnaire-confirmation.ftl", data)
        }

        
        // Doctor landing page
        get("/funksjonsvurdering/doctor") {
            call.respondTemplate("funksjonsvurdering/doctor.ftl")
        }




    }

}
