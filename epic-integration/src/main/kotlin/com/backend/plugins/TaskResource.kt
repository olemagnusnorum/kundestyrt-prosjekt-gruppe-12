package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Task

class TaskResource(server: String = "public") {

    // The base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val client = HttpClient()
    private val jsonParser: IParser = FhirContext.forR4().newJsonParser()

    // {patientId: [questionnaire]}
    var inbox: MutableMap<String, MutableList<Task>> = mutableMapOf()

    /**
     * Create task from patient and questionnaire.
     * @param [patientId] the id of the patient the task is for
     * @param [questionnaireId] the id of the questionnaire the task focuses on
     */
    suspend fun create(patientId: String, questionnaireId: String) {
        val task = Task()

        task.`for` = Reference().setReference("Patient/$patientId")
        task.focus = Reference().setReference("Questionnaire/$questionnaireId")
        task.status = Task.TaskStatus.REQUESTED
        task.intent = Task.TaskIntent.ORDER
        task.description = "Questions from NAV to be answered by doctor."

        client.post("$baseURL/Task"){
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(task)
        } as HttpResponse
    }

    /**
     * Add Task to local inbox when it arrives via subscription
     * @param [taskJson] the json formatted Task-string
     */
    fun addToInbox(taskJson: String) {
        val task = jsonParser.parseResource(Task::class.java, taskJson)
        val patientId = task.`for`.reference.substringAfter("/")

        if (inbox.containsKey(patientId)) inbox[patientId]?.add(task)
        else inbox[patientId] = mutableListOf(task)
    }
}