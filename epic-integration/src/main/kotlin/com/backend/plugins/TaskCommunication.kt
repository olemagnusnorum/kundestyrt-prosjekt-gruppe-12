package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Task

class TaskCommunication(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    private val jsonParser: IParser = ctx.newJsonParser()

    private val questionnaireCommunication = QuestionnaireCommunication("local")

    //{patientId: [questionnaire]}
    var inbox: MutableMap<String, MutableList<Task>> = mutableMapOf()

    /**
     * Create task from patient and questionnaire.
     */
    suspend fun createTask(patientId: String, questionnaireId: String): HttpResponse {

        val task = Task()

        task.`for` = Reference().setReference("Patient/$patientId")

        task.focus = Reference().setReference("Questionnaire/$questionnaireId")

        task.status = Task.TaskStatus.REQUESTED

        task.intent = Task.TaskIntent.ORDER

        task.description = "Questions from NAV to be answered by doctor."

        val response: HttpResponse = client.post("$baseURL/Task"){
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(task)
        }

        return response
    }

    /**
     * Add Task to local inbox when it arrives via subscription
     */
    fun addToInbox(json: String) {
        val task = jsonParser.parseResource(Task::class.java, json)
        val patientId = task.`for`.reference.substringAfter("/")

        if (inbox.containsKey(patientId)) {
            inbox[patientId]?.add(task)
        }
        else {
            var newList = mutableListOf<Task>(task)
            inbox[patientId] = newList
        }
    }
}