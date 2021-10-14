package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.runBlocking


fun Application.funksjonsvurderingRoute() {

    val questionnaireCommunication = QuestionnaireCommunication()

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
            val jsonResponse = runBlocking { questionnaireCommunication.createQuestionnaire(params) }

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

            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl")
        }

        post("/funksjonsvurdering/doctor-inbox") {

            val patientId: String = call.receiveParameters()["patientId"]!!

            //TODO: Get all questionnaires associated with patientId, put them in a list and send them as "questionnaires"
            //For now hardcoded for testing
            val questionnaires = mutableListOf(questionnaireCommunication.getQuestionnaire("2641197"))

            println(questionnaires[0].text)

            val data = mapOf("patientId" to patientId, "questionnaires" to questionnaires)
            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl", data)
        }

        //functional analysis  | Helseplattformen questionnaire
        //Called when doctor opens a questionnaire
        get("funksjonsvurdering/doctor-inbox/Questionnaire/{questionnaireId}/_history/1") {
            val questionnaireId: String = call.parameters["questionnaireId"]!!

            val data = mapOf("questionnaire" to questionnaireCommunication.getQuestionnaire(questionnaireId))

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

            questionnaireCommunication.createQuestionnaireResponse(questionnaireCommunication.getQuestionnaire(questionnaireId), answerList)
            call.respondRedirect("/funksjonsvurdering/doctor-inbox")
        }

        //new questionnaire site
        post("/create-questionnaire"){
            val params = call.receiveParameters()

            val question1 = params["question1"]!!
            val question2 = params["question2"]!!


            val jsonResponse = runBlocking { questionnaireCommunication.createQuestionnaire(params) }
            val data = mapOf("response" to jsonResponse)
            //testing inbox function

            navInbox.addToInbox("Questionnaire", jsonResponse)
            call.respondTemplate("create-questionnaire-confirmation.ftl", data)
        }

        //new questionnaire site
        get("/funksjonsvurdering/read-questionnaire-response"){

            // Mathis made these
            val questionnaireID = "2641197"
            val questionnaireResponseID = "2644277"

            // Extract questionnaire and questionnaireResponse
            val questionnaire = questionnaireCommunication.getQuestionnaire(questionnaireID)
            val questionnaireResponse = questionnaireCommunication.getQuestionnaireResponse(questionnaireResponseID)

            // Extract questions and answers
            val questions = questionnaireCommunication.getQuestionnaireQuestions(questionnaire)
            val answers = questionnaireCommunication.getQuestionnaireAnswers(questionnaireResponse)

            // Map data so we can display it on the Front end
            val data = mapOf("questions" to questions, "answers" to answers)
            call.respondTemplate("/funksjonsvurdering/read-questionnaire-response.ftl", data)
        }
    }

}
