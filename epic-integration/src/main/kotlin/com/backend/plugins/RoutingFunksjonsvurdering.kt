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

            val questionnaireResponses = questionnaireResponseCommunication.inbox[patientId]

            // Getting questionnaire titles as they are not in questionnaireResponses
            val questionnaireTitles = mutableListOf<String>()

            if (questionnaireResponses != null) {
                for (questionnaireResponse in questionnaireResponses) {
                    questionnaireTitles.add(questionnaireCommunication.getQuestionnaire(questionnaireResponse.questionnaire.substringAfter("/")).title)
                }
            }

            val data = mapOf("patient" to patient, "questionnaireResponses" to questionnaireResponses, "questionnaireTitles" to questionnaireTitles)
            call.respondTemplate("funksjonsvurdering/nav.ftl", data)
        }

        // Nav choose questionnaireResponse
        get("/funksjonsvurdering/nav/QuestionnaireResponse/{questionnaireResponseId}/_history/1"){

            // Getting questionnaireResponse
            val questionnaireResponseId: String = call.parameters["questionnaireResponseId"]!!
            val questionnaireResponse = questionnaireResponseCommunication.getQuestionnaireResponse(questionnaireResponseId)

            //Getting questionnaire
            val questionnaire = questionnaireCommunication.getQuestionnaire(questionnaireResponse.questionnaire.substringAfter(("/")))

            // Extract questions and answers
            val questions = questionnaireCommunication.getQuestionnaireQuestions(questionnaire)
            val answers = questionnaireResponseCommunication.getQuestionnaireAnswers(questionnaireResponse)

            // Map data so we can display it on the Front end
            val data = mapOf("questions" to questions, "answers" to answers)
            call.respondTemplate("/funksjonsvurdering/read-questionnaire-response.ftl", data)
        }

        // Nav create questionnaire
        get("/funksjonsvurdering/create-questionnaire/Patient/{patientId}/_history/1") {
            val patientId: String = call.parameters["patientId"]!!

            val data = mapOf("patientId" to patientId)

            call.respondTemplate("funksjonsvurdering/create-questionnaire.ftl", data)
        }

        // Nav create questionnaire confirmation
        post("/funksjonsvurdering/create-questionnaire"){

            val params = call.receiveParameters()
            val patientId: String = params["patientId"]!!

            val jsonResponse = questionnaireCommunication.createQuestionnaire(params, patientId)
            val data = mapOf("response" to jsonResponse)

            call.respondTemplate("funksjonsvurdering/create-questionnaire-confirmation.ftl", data)
        }

        //––––––––––––––––– Doctor –––––––––––––––––

        // Doctor choose patient page
        get("/funksjonsvurdering/doctor-inbox") {

            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl")
        }

        // Doctor landing page after choosing patient
        post("/funksjonsvurdering/doctor-inbox") {

            val patientId: String = call.receiveParameters()["patientId"]!!
            val patient = patientCommunication.readPatient(patientId)

            val questionnaires = questionnaireCommunication.inbox[patientId]

            val data = mapOf("patient" to patient, "questionnaires" to questionnaires)
            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl", data)
        }

        // Doctor choose questionnaire
        get("funksjonsvurdering/doctor-inbox/Questionnaire/{questionnaireId}/_history/1") {
            val questionnaireId: String = call.parameters["questionnaireId"]!!

            val data = mapOf("questionnaire" to questionnaireCommunication.getQuestionnaire(questionnaireId))

            call.respondTemplate("funksjonsvurdering/create-questionnaire-response.ftl", data)
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

            val patientId = questionnaire.identifier[0].value.substringAfter("/")

            questionnaireResponseCommunication.createQuestionnaireResponse(questionnaire, answerList, patientId)

            //This is where q should be deleted, but need to get patientId
            questionnaireCommunication.inbox.remove(patientId)

            call.respondRedirect("/funksjonsvurdering/doctor-inbox")
        }
    }
}
