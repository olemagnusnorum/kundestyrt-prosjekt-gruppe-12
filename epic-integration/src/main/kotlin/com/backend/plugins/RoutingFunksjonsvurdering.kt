package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.netty.handler.codec.http.HttpResponseStatus
import org.hl7.fhir.r4.model.Questionnaire

fun Application.funksjonsvurderingRoute(questionnaireResponseCommunication: QuestionnaireResponseCommunication, questionnaireCommunication: QuestionnaireCommunication) {

    val patientCommunication = PatientCommunication("local")
    val taskCommunication = TaskCommunication("local")
    val pdfHandler = PdfHandler()
    var lastPatient: String = "5"

    routing {
        get("/") {
            call.respondTemplate("index.ftl")
        }

        // Landing page - navigation
        get("/funksjonsvurdering") {
            call.respondTemplate("funksjonsvurdering/index.ftl")
        }
        //fhir subscription endpoint for questionnaireResponse subscription
        put("funksjonsvurdering/questionnaireResponse-subscription/{...}"){
            val body = call.receive<String>()
            println("message received")
            questionnaireResponseCommunication.addToInbox(body)

            val questionnaireResponse = questionnaireResponseCommunication.parseQuestionnaireResponse(body)
            val patient = patientCommunication.readPatient(questionnaireResponse.subject.reference.substringAfter("/"))

            val header = "NAV respons fra Helseplattformen\n" +
                        "Dato: ${questionnaireResponse.meta.lastUpdated}\n" +
                        "Pasient: ${patient.name[0].given[0]} ${patient.name[0].family}"

            pdfHandler.writeToPdf(header, questionnaireResponseCommunication.jsonParser.setPrettyPrint(true).encodeResourceToString(questionnaireResponse), "${patient.identifier[0].value}.pdf")

            call.respond(HttpResponseStatus.CREATED)
        }
        //fhir subscription endpoint for task subscription
        put("funksjonsvurdering/task-subscription/{...}"){
            val body = call.receive<String>()
            println("message received")
            taskCommunication.addToInbox(body)
            call.respond(HttpResponseStatus.CREATED)
        }

        //–––––––––––––––––  Nav  –––––––––––––––––

        // Nav choose patient page
        get("/funksjonsvurdering/nav") {
            call.respondTemplate("funksjonsvurdering/nav.ftl")
        }

        // Nav landing page after choosing patient
        post("/funksjonsvurdering/nav") {

            val patientIdentifier = call.receiveParameters()["patientId"]!!
            val bundleString = patientCommunication.patientSearch(identifier = patientIdentifier)
            val patient = patientCommunication.parseBundleXMLToPatient(bundleString, false)
            val patientId = patient?.id!!.split("/")[5]

            val questionnaireResponses = questionnaireResponseCommunication.inbox[patientId]

            // Getting questionnaire titles as they are not in questionnaireResponses
            val questionnaireTitles = mutableListOf<String>()

            if (questionnaireResponses != null) {
                for (questionnaireResponse in questionnaireResponses) {
                    questionnaireTitles.add(questionnaireCommunication.getQuestionnaire(questionnaireResponse.questionnaire.substringAfter("/")).title)
                }
            }

            val data = mapOf("patient" to patient,
                    "patientId" to patientId,
                    "questionnaireResponses" to questionnaireResponses,
                    "questionnaireTitles" to questionnaireTitles,
                    "predefinedQuestionnaires" to questionnaireCommunication.predefinedQuestionnaires)
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

            // Getting patient
            val patientId = questionnaireResponse.subject.reference.substringAfter("/")

            // Map data so we can display it on the Front end
            val data = mapOf("questions" to questions, "answers" to answers, "patientId" to patientId)
            call.respondTemplate("/funksjonsvurdering/read-questionnaire-response.ftl", data)
        }

        // NAV send predefined questionnaire
        post("/funksjonsvurdering/create-predefined-questionnaire") {

            val params = call.receiveParameters()
            val patientId: String = params["patientId"]!!.split("/")[5]
            val questionnaireId: String = params["questionnaireId"]!!

            taskCommunication.createTask(patientId, questionnaireId) //Should trigger subscription

            call.respondTemplate("funksjonsvurdering/nav.ftl")
        }

        //––––––––––––––––– Doctor –––––––––––––––––

        // Doctor choose patient page
        get("/funksjonsvurdering/doctor-inbox") {

            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl")
        }

        // Doctor landing page after choosing patient
        post("/funksjonsvurdering/doctor-inbox") {

            val patientIdentifier: String = call.receiveParameters()["patientId"]!!
            val patientString = patientCommunication.patientSearch(identifier = patientIdentifier)
            val patient = patientCommunication.parseBundleXMLToPatient(patientString, false)
            val patientId = patient?.id!!.split("/")[5]
            lastPatient = patientId

            // Get all questionnaires related to patient from task-inbox
            val tasks = taskCommunication.inbox[patientId]

            if (tasks != null) {
                val questionnaires = mutableListOf<Questionnaire>()

                for (task in tasks) {
                    questionnaires.add(questionnaireCommunication.getQuestionnaire(task.focus.reference.substringAfter("/")))
                }

                val data = mapOf("patient" to patient, "questionnaires" to questionnaires)
                call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl", data)
            } else {

                val data = mapOf("patient" to patient)
                call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl", data)
            }

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

            questionnaireResponseCommunication.createQuestionnaireResponse(questionnaire, answerList, lastPatient)

            // Q deleted when answered
            taskCommunication.inbox[lastPatient]?.removeAll {it.focus.reference == "Questionnaire/$questionnaireId"}

            call.respondRedirect("/funksjonsvurdering/doctor-inbox")
        }
    }
}
