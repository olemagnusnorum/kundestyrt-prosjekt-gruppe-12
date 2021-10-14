package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.runBlocking


fun Application.funksjonsvurderingRoute() {

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

        //functional analysis | Helseplattformen inbox
        //Called when doctor is accessing inbox
        get("/funksjonsvurdering/doctor-inbox") {

            //TODO: Get all questionnaires associated with patientId, put them in a list and send them as "questionnaires"
            //For now hardcoded for testing
            val questionnaires = mutableListOf(epicCommunication.getQuestionnaire("2641197"))

            println(questionnaires[0].text)

            val data = mapOf("patientId" to "2641197", "questionnaires" to questionnaires)
            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl", data)
            //}
        }

        //functional analysis  | Helseplattformen questionnaire
        //Called when doctor opens a questionnaire
        get("funksjonsvurdering/doctor-inbox/Questionnaire/{questionnaireId}/_history/1") {
            val questionnaireId: String = call.parameters["questionnaireId"]!!

            val data = mapOf("questionnaire" to epicCommunication.getQuestionnaire(questionnaireId))

            call.respondTemplate("funksjonsvurdering/questionnaireResponse.ftl", data)
        }

        //functional analysis | Helseplattformen questionnaire response
        //Called when doctor response to a questionnaire with a questionnaire response
        post("funksjonsvurdering/createQuestionnaireResponse/Questionnaire/{questionnaireId}/_history/1") {
            val questionnaireId: String = call.parameters["questionnaireId"]!!
            val params = call.receiveParameters()
            val answerList = mutableListOf<String>()

            println("Creating qr...")
            println("PARAMETERS ${params}")

            //BAHHH frick it, will do it like this for now
            answerList.add(params["answer1"]!!)
            answerList.add(params["answer2"]!!)
            //answerList.add(params["answer3"]!!)

            epicCommunication.createQuestionnaireResponse(epicCommunication.getQuestionnaire(questionnaireId), answerList)
            call.respondRedirect("/funksjonsvurdering/doctor-inbox")
        }
    }

}
