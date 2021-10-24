package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.hl7.fhir.r4.model.*

class QuestionnaireResponseCommunication(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    private val jsonParser: IParser = ctx.newJsonParser()

    //{patientId: [questionnaire]}
    var inbox: MutableMap<String, MutableList<QuestionnaireResponse>> = mutableMapOf()

    /**
     * Generates a QuestionnaireResponse to a specific Questionnaire
     * @param
     * @param questionnaire Questionnaire the response is related to
     * @return http response, not QuestionnaireResponse
     */
    suspend fun createQuestionnaireResponse(questionnaire: Questionnaire, questionsList: MutableList<String>, patientId: String = "2559067"): HttpResponse {

        // Create empty template
        val questionnaireResponse = QuestionnaireResponse()

        //Link Questionnaire
        questionnaireResponse.questionnaire = questionnaire.id.substringBeforeLast("/").substringBeforeLast("/")

        questionnaireResponse.subject = Reference("Patient/$patientId")


        //Put answers in Item and add them to QR
        val item = mutableListOf<QuestionnaireResponse.QuestionnaireResponseItemComponent>()

        for (i in 0..questionnaire.item.size-1) {

            item.add(QuestionnaireResponse.QuestionnaireResponseItemComponent())
            item[i].linkId = questionnaire.item[i].id
            item[i].text = questionnaire.item[i].text

            val answerComponent = mutableListOf<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent>()
            answerComponent.add(QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent())
            answerComponent[0].value = Coding(
                "some.system",
                questionsList[i], questionsList[i])

            item[i].answer = answerComponent
        }

        questionnaireResponse.item = item

        //post the questionnaireResponse to the server
        val response: HttpResponse = client.post("$baseURL/QuestionnaireResponse"){
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(questionnaireResponse)
        }

        return response
    }

    /**
     * Function to get a QuestionnaireResponse resource.
     * @param id is the id of the QR to get.
     * @return QuestionnaireResponse resource
     */
    suspend fun getQuestionnaireResponse(id: String, format: String = "json"): QuestionnaireResponse {
        val response: HttpResponse = client.get("$baseURL/QuestionnaireResponse/$id?_format=$format") {}
        return jsonParser.parseResource(QuestionnaireResponse::class.java, response.receive<String>())
    }

    /**
     * Function to get all QuestionnaireResponses on the server.
     * @return Bundle resource with QuestionnaireResponses in entry.
     */
    suspend fun getAllQuestionnaireResponses(): Bundle {
        val response: HttpResponse = client.get("$baseURL/QuestionnaireResponse?_format=json") {}
        return jsonParser.parseResource(Bundle::class.java, response.receive<String>())
    }

    /**
     * Finds the Answers of a FHIR QuestionnaireResponse object
     * @return listOfAnswers a list of Strings containing the answers
     */
    fun getQuestionnaireAnswers(questionnaireResponse: QuestionnaireResponse) : List<String> {
        val listOfAnswers: MutableList<String> = mutableListOf()
        for (item in questionnaireResponse.item) {
            listOfAnswers.add(item.answer[0].valueCoding.code)
        }
        return listOfAnswers
    }

    /**
     * Add questionnaireResponse to local inbox when it arrives via subscription
     */
    fun addToInbox(json: String) {
        val questionnaireResponse = jsonParser.parseResource(QuestionnaireResponse::class.java, json)
        val patientId = questionnaireResponse.subject.reference.substringAfter("/")

        if (inbox.containsKey(patientId)) {
            inbox[patientId]?.add(questionnaireResponse)
        }
        else {
            var newList = mutableListOf<QuestionnaireResponse>(questionnaireResponse)
            inbox[patientId] = newList
        }
    }
}
