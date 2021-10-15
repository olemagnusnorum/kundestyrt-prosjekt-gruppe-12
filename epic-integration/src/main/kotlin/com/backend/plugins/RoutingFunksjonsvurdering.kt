package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Questionnaire


fun Application.funksjonsvurderingRoute() {

    val questionnaireCommunication = QuestionnaireCommunication()
    val questionnaireResponseCommunication = QuestionnaireResponseCommunication()
    val patientCommunication = PatientCommunication()

    routing {

        // Landing page - navigation
        get("/funksjonsvurdering") {
            call.respondTemplate("funksjonsvurdering/index.ftl")
        }

        //–––––––––––––––––  Nav  –––––––––––––––––

        // Nav choose patient page
        get("/funksjonsvurdering/nav") {
            call.respondTemplate("funksjonsvurdering/nav.ftl")
        }

        // Nav landing page after choosing patient
        post("/funksjonsvurdering/nav") {

            val patientId = call.receiveParameters()["patientId"]!!
            val patient = patientCommunication.readPatient(patientId)

            //TODO: Get all questionnaires associated with patientId, put them in a list and send them as "questionnaires"
            //For now hardcoded for testing
            val questionnaireResponses = questionnaireResponseCommunication.inbox[patientId]

            //testing 2559067

            val data = mapOf("patient" to patient, "questionnaireResponses" to questionnaireResponses)
            call.respondTemplate("funksjonsvurdering/nav.ftl", data)
        }

        // Nav choose questionnaireResponse
        get("/funksjonsvurdering/nav/QuestionnaireResponse/{questionnaireResponseId}/_history/1"){

            // Getting questionnaireResponse
            val questionnaireResponseId: String = call.parameters["questionnaireResponseId"]!!
            val questionnaireResponse = questionnaireResponseCommunication.getQuestionnaireResponse(questionnaireResponseId)

            println(questionnaireResponse.questionnaire)

            //Getting questionnaire
            val questionnaire = questionnaireCommunication.getQuestionnaire(questionnaireResponse.questionnaire)

            // Extract questions and answers
            val questions = questionnaireCommunication.getQuestionnaireQuestions(questionnaire)
            val answers = questionnaireResponseCommunication.getQuestionnaireAnswers(questionnaireResponse)

            // Map data so we can display it on the Front end
            val data = mapOf("questions" to questions, "answers" to answers)
            call.respondTemplate("/funksjonsvurdering/read-questionnaire-response.ftl", data)
        }

        // Nav create questionnaire
        get("/funksjonsvurdering/create-questionnaire") {
            call.respondTemplate("funksjonsvurdering/create-questionnaire.ftl")
        }

        // Nav create questionnaire confirmation
        post("/funksjonsvurdering/create-questionnaire"){
            val params = call.receiveParameters()

            val jsonResponse = runBlocking { questionnaireCommunication.createQuestionnaire(params) }
            val data = mapOf("response" to jsonResponse)
            //testing inbox function

            navInbox.addToInbox("Questionnaire", jsonResponse)
            call.respondTemplate("create-questionnaire-confirmation.ftl", data)
        }

        //––––––––––––––––– Doctor –––––––––––––––––

        // Doctor landing page, is not currently used
        get("/funksjonsvurdering/doctor") {
            call.respondTemplate("funksjonsvurdering/doctor.ftl")
        }

        // Doctor choose patient page
        get("/funksjonsvurdering/doctor-inbox") {

            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl")
        }

        // Doctor landing page after choosing patient
        post("/funksjonsvurdering/doctor-inbox") {

            val patientId: String = call.receiveParameters()["patientId"]!!

            //TODO: Get all questionnaires associated with patientId, put them in a list and send them as "questionnaires"
            //For now hardcoded for testing
            println(questionnaireCommunication.inbox[patientId])

            //Should be gotten from inbox: questionnaireCommunication.inbox[patientId]
            val questionnaires = mutableListOf(questionnaireCommunication.getQuestionnaire("2645039"))

            val data = mapOf("patientId" to patientId, "questionnaires" to questionnaires)
            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl", data)
        }

        // Doctor choose questionnaire
        get("funksjonsvurdering/doctor-inbox/Questionnaire/{questionnaireId}/_history/1") {
            val questionnaireId: String = call.parameters["questionnaireId"]!!

            val data = mapOf("questionnaire" to questionnaireCommunication.getQuestionnaire(questionnaireId))

            call.respondTemplate("funksjonsvurdering/questionnaireResponse.ftl", data)
        }

        // Doctor create questionnaireResponse
        post("funksjonsvurdering/createQuestionnaireResponse/Questionnaire/{questionnaireId}/_history/1") {
            val questionnaireId: String = call.parameters["questionnaireId"]!!
            val questionnaire: Questionnaire = questionnaireCommunication.getQuestionnaire(questionnaireId)
            val params = call.receiveParameters()
            val answerList = mutableListOf<String>()

            answerList.add(params["answer1"]!!)
            answerList.add(params["answer2"]!!)
            answerList.add(params["answer3"]!!)

            questionnaireResponseCommunication.createQuestionnaireResponse(questionnaire, answerList)
            call.respondRedirect("/funksjonsvurdering/doctor-inbox")
        }
    }
}
