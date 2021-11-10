package com.backend.plugins.funksjonsvurdering

import com.backend.patientResource
import com.backend.questionnaireResource
import com.backend.questionnaireResponseResource
import com.backend.taskResource
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.hl7.fhir.r4.model.Questionnaire

fun Application.doctorRoute() {
    routing {
        // Doctor choose patient page
        get("/funksjonsvurdering/doctor-inbox") {
            call.respondTemplate("funksjonsvurdering/doctor-inbox.ftl")
        }

        // Doctor landing page after choosing patient
        post("/funksjonsvurdering/doctor-inbox") {
            val patientIdentifier: String = call.receiveParameters()["patientId"]!!
            val patient = patientResource.search(identifier = patientIdentifier)
            val patientId = patient!!.id.split("/")[5]
            lastPatient = patientId

            // Get all questionnaires related to patient from task-inbox
            val tasks = taskResource.inbox[patientId]

            if (tasks != null) {
                val questionnaires = mutableListOf<Questionnaire>()

                for (task in tasks) {
                    questionnaires.add(questionnaireResource.read(task.focus.reference.substringAfter("/")))
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
            val data = mapOf("questionnaire" to questionnaireResource.read(questionnaireId))
            call.respondTemplate("funksjonsvurdering/create-questionnaire-response.ftl", data)
        }

        // Doctor create questionnaireResponse
        post("funksjonsvurdering/createQuestionnaireResponse/Questionnaire/{questionnaireId}/_history/1") {
            val questionnaireId: String = call.parameters["questionnaireId"]!!
            val questionnaire: Questionnaire = questionnaireResource.read(questionnaireId)
            val params = call.receiveParameters()
            val answerList = mutableListOf<String>()

            answerList.add(params["answer1"]!!)
            answerList.add(params["answer2"]!!)
            answerList.add(params["answer3"]!!)

            questionnaireResponseResource.create(questionnaire, answerList, lastPatient)

            // Q deleted when answered
            taskResource.inbox[lastPatient]?.removeAll {it.focus.reference == "Questionnaire/$questionnaireId"}
            call.respondRedirect("/funksjonsvurdering/doctor-inbox")
        }
    }
}
