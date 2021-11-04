package com.backend.plugins.funksjonsvurdering

import com.backend.patientResource
import com.backend.questionnaireResource
import com.backend.questionnaireResponseResource
import com.backend.taskResource
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.routing.*
import org.hl7.fhir.r4.model.Questionnaire

fun Application.navRoute() {
    routing {
        // Nav choose patient page
        get("/funksjonsvurdering/nav") {
            call.respondTemplate("funksjonsvurdering/nav.ftl")
        }

        // Nav landing page after choosing patient
        post("/funksjonsvurdering/nav") {
            val patientIdentifier = call.receiveParameters()["patientId"]!!
            val patient = patientResource.search(identifier = patientIdentifier)
            val patientId = patient!!.id.split("/")[5]

            val questionnaireResponses = questionnaireResponseResource.inbox[patientId]

            // Getting questionnaire titles as they are not in questionnaireResponses
            val questionnaireTitles = mutableListOf<String>()

            if (questionnaireResponses != null) {
                for (questionnaireResponse in questionnaireResponses) {
                    questionnaireTitles.add(questionnaireResource.read(questionnaireResponse.questionnaire.substringAfter("/")).title)
                }
            }

            val questionnaireIds = mutableListOf<Pair<Questionnaire, String>>()
            for (q in questionnaireResource.predefinedQuestionnaires) {
                val tokens = q.id.split("/")
                questionnaireIds.add(Pair(q, tokens[tokens.lastIndex-2]))
            }

            val data = mapOf("patient" to patient,
                "patientId" to patientId,
                "questionnaireResponses" to questionnaireResponses,
                "questionnaireTitles" to questionnaireTitles,
                "predefinedQuestionnaires" to questionnaireIds
            )
            call.respondTemplate("funksjonsvurdering/nav.ftl", data)
        }

        // Nav choose questionnaireResponse
        get("/funksjonsvurdering/nav/QuestionnaireResponse/{questionnaireResponseId}/_history/1"){
            // Getting questionnaireResponse
            val questionnaireResponseId: String = call.parameters["questionnaireResponseId"]!!
            val questionnaireResponse = questionnaireResponseResource.read(questionnaireResponseId)

            // Getting questionnaire
            val questionnaire = questionnaireResource.read(questionnaireResponse.questionnaire.substringAfter(("/")))

            // Extract questions and answers
            val questions = questionnaireResource.retrieveQuestions(questionnaire)
            val answers = questionnaireResponseResource.retrieveAnswers(questionnaireResponse)

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

            taskResource.create(patientId, questionnaireId) //Should trigger subscription
            call.respondTemplate("funksjonsvurdering/nav.ftl")
        }
    }
}
