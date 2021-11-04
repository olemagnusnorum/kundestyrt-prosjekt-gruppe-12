package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuestionnaireResource(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val client = HttpClient()
    private val jsonParser: IParser = FhirContext.forR4().newJsonParser()

    var predefinedQuestionnaires = mutableListOf<Questionnaire>()

    fun createDefaultQuestionnaires() {
        var questions = Parameters.build {
            append("question1", "Kan pasienten høre?")
            append("question2", "Kan pasienten se?")
            append("question3", "Kan pasienten prate?")
        }

        runBlocking { predefinedQuestionnaires.add(read(create(questions, "Sanser")!!)) }

        questions = Parameters.build {
            append("question1", "Kan pasienten ligge?")
            append("question2", "Kan pasienten stå?")
            append("question3", "Kan pasienten gå?")
        }

        runBlocking { predefinedQuestionnaires.add(read(create(questions, "Fysiske evner")!!)) }

        questions = Parameters.build {
            append("question1", "Kan pasienten spille trompet?")
            append("question2", "Kan pasienten løse kryssord?")
            append("question3", "Kan pasienten danse cancan?")
        }

        runBlocking { predefinedQuestionnaires.add(read(create(questions, "Annet")!!)) }
    }

    /**
     * Function to retrieve a Questionnaire resource.
     * @param [questionnaireId] the id of the Questionnaire to retrieve.
     * @return Questionnaire resource
     */
    suspend fun read(questionnaireId: String): Questionnaire {
        val response: HttpResponse = client.get("$baseURL/Questionnaire/$questionnaireId?_format=json") {}
        return jsonParser.parseResource(Questionnaire::class.java, response.receive<String>())
    }

    /**
     * Function to create a questionnaire and save the questionnaire to the fhir server.
     * In the future, this function should take in parameters, for the different values.
     * @param [questions] params of the questions. Get these from navigation. When you click
     * the "Register questionnaire" button you receive the params to send in.
     * @param [title] the title of the questionnaire
     * @return the questionnaireId of the created questionnaire if successful, else null
     */
    suspend fun create(questions: Parameters, title: String): String? {
        val questionnaire = Questionnaire()

        // The date is set to the current date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = LocalDate.now().format(formatter)
        val date = SimpleDateFormat("yyyy-MM-dd").parse(dateString)

        questionnaire.name = "NavQuestionnaire"
        questionnaire.title = title
        questionnaire.status = Enumerations.PublicationStatus.ACTIVE
        questionnaire.date = date
        questionnaire.publisher = "NAV"
        questionnaire.description = "Questions about funksjonsvurdering"

        // Set the items for the questionnaire (the questions)
        var number = 0
        val items = mutableListOf<Questionnaire.QuestionnaireItemComponent>()

        questions.forEach { t, question ->
            if (t != "patientId") {
                number++
                val item = Questionnaire.QuestionnaireItemComponent()
                item.linkId = number.toString()
                item.text = question[0]
                item.type = Questionnaire.QuestionnaireItemType.STRING
                items.add(item)
            }
        }

        questionnaire.item = items

        // Post the questionnaire to the server
        val response: HttpResponse = client.post("$baseURL/Questionnaire"){
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(questionnaire)
        }

        if (response.headers["Location"] != null) {
            // Return the questionnaireId if a questionnaire was created
            return response.headers["Location"]!!.split("/")[5]
        }

        return null
    }

    /**
     * Finds the Questions of a FHIR Questionnaire object
     * @param [questionnaire] the questionnaire to retrieve questions from
     * @return a list of Strings containing the questions
     */
    fun retrieveQuestions(questionnaire: Questionnaire) : List<String> {
        val listOfQuestions: MutableList<String> = mutableListOf()
        for (item in questionnaire.item)
            listOfQuestions.add(item.text)
        return listOfQuestions
    }

    /**
     * Retrieve all questionnaires in the fhir server
     * @return a list of all questionnaires
     */
    suspend fun getAll() : MutableList<Questionnaire> {
        val response: HttpResponse = client.get("$baseURL/Questionnaire?_format=json")
        val bundle = jsonParser.parseResource(Bundle::class.java, response.receive<String>())
        val questionnaires = mutableListOf<Questionnaire>()

        // Convert all questionnaires to resources
        for (bundleComponent in bundle.entry) {
            val questionnaire = bundleComponent.resource as Questionnaire
            questionnaires.add(questionnaire)
        }

        return questionnaires
    }
}
