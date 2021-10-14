package com.backend.plugins
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse

class QuestionnaireCommunication(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir/"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()


    /**
     * Function to get a Questionnaire resource.
     * @param id is the id of the Questionnaire to get.
     * @return QuestionnaireResponse resource
     */
    suspend fun getQuestionnaireResource(id: String, format: String = "json"): Questionnaire {
        val response: HttpResponse = client.get("$baseURL/Questionnaire/$id?_format=$format") {}
        val jsonParser: IParser = ctx.newJsonParser()
        return jsonParser.parseResource(Questionnaire::class.java, response.receive<String>())
    }

    /**
     * Function to get a QuestionnaireResponse resource.
     * @param id is the id of the QR to get.
     * @return QuestionnaireResponse resource
     */
    suspend fun getQuestionnaireResponseResource(id: String, format: String = "json"): QuestionnaireResponse {
        val response: HttpResponse = client.get("$baseURL/QuestionnaireResponse/$id?_format=$format") {}
        val jsonParser: IParser = ctx.newJsonParser()
        return jsonParser.parseResource(QuestionnaireResponse::class.java, response.receive<String>())
    }


    /**
     * Finds the Questions of a FHIR Questionnaire object
     * @return listOfQuestions a list of Strings containing the questions
     */
    fun getQuestionnaireQuestions(questionnaire: Questionnaire) : List<String> {
        val listOfQuestions: MutableList<String> = mutableListOf()
        for (item in questionnaire.item) {
            listOfQuestions.add(item.text)
        }
        return listOfQuestions
    }

}
