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
import java.util.*

class QuestionnaireCommunication(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir/"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    private val jsonParser: IParser = ctx.newJsonParser()


    /**
     * Function to create a questionnaire and save the questionnaire to fhir server.
     * In the future, this function should take in parameters, for the
     * different values.
     * @param questions Params of the questions. Get these from navigation. When you click
     * the "Register questionnaire" button you receive the params to send in.
     * @return id of the created questionnaire or "EMPTY" if a questionnaire was not created.
     */
    suspend fun createQuestionnaire(questions: Parameters): String{

        val questionnaire = Questionnaire()

        // The date is set to the current date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = LocalDate.now().format(formatter)
        val date = SimpleDateFormat("yyyy-MM-dd").parse(dateString)

        questionnaire.setName("NavQuestionnaire")
        questionnaire.setTitle("Nav questionaire: Funksjonsvurdering")
        questionnaire.setStatus(Enumerations.PublicationStatus.ACTIVE)
        questionnaire.setDate(date)
        questionnaire.setPublisher("NAV")
        questionnaire.setDescription("Questions about funksjonsvurdering")

        // Set the items for the questionnaire (the questions)
        var number: Int = 0
        var items = mutableListOf<Questionnaire.QuestionnaireItemComponent>()

        questions.forEach { t, question ->
            number++
            var item = Questionnaire.QuestionnaireItemComponent()
            item.setLinkId(number.toString())
            println(question[0])
            item.setText(question[0])
            item.setType(Questionnaire.QuestionnaireItemType.STRING)
            items.add(item)
        }

        questionnaire.setItem(items)

        // Set the identifier. Should be on the format UUID/patientID.
        // This allows us to connect a questionnaire to a patient.
        // TODO: figure out how to search for a questionnaire, this might not work
        val identifier = Identifier()
        val uuid = UUID.randomUUID().toString()
        identifier.setValue("$uuid/1244780")
        questionnaire.setIdentifier(mutableListOf(identifier))

        val questionnaireJson = jsonParser.encodeResourceToString(questionnaire)

        //post the questionnaire to the server
        val response: HttpResponse = client.post("$baseURL/Questionnaire"){

            contentType(ContentType.Application.Json)
            body = questionnaireJson
        }

        val responseString = response.receive<String>()
        println("HEADERS: ${response.headers}")

        if (response.headers["Location"] != null) {
            println(response.headers["Location"])
            var responseId = response.headers["Location"]!!.split("/")[5]
            return responseId
        }

        return "EMPTY"
    }

    /**
     * Generates a QuestionnaireResponse to a specific Questionnaire
     * @param
     * @param questionnaire Questionnaire the response is related to
     * @return http response, not QuestionnaireResponse
     */
    suspend fun createQuestionnaireResponse(questionnaire: Questionnaire, questionsList: MutableList<String>): HttpResponse {

        // Create empty template
        val questionnaireResponse = QuestionnaireResponse()

        //Link Questionnaire
        questionnaireResponse.questionnaire = questionnaire.id.substringBeforeLast("/").substringBeforeLast("/")

        //TODO: Link patient. Where to get patient id? Probably send as new parameter

        questionnaireResponse.subject = Reference("Patient/13")

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

        println(jsonParser.setPrettyPrint(true).encodeResourceToString(questionnaireResponse))

        //post the questionnaireResponse to the server
        val response: HttpResponse = client.post("$baseURL/QuestionnaireResponse"){
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(questionnaireResponse)
        }

        println(response.headers["Location"])

        return response
    }


    /**
     * Function to get a Questionnaire resource.
     * @param id is the id of the Questionnaire to get.
     * @return QuestionnaireResponse resource
     */
    suspend fun getQuestionnaire(id: String, format: String = "json"): Questionnaire {
        val response: HttpResponse = client.get("$baseURL/Questionnaire/$id?_format=$format") {}
        return jsonParser.parseResource(Questionnaire::class.java, response.receive<String>())
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

    /**
     * Finds the Answers of a FHIR QuestionnaireResponse object
     * @return listOfAnswers a list of Strings containing the answers
     */
    fun getQuestionnaireAnswers(questionnaireResponse: QuestionnaireResponse) : List<String> {
        val listOfQuestions: MutableList<String> = mutableListOf()
        for (item in questionnaireResponse.item) {
            listOfQuestions.add(item.answer[0].valueCoding.code)
        }
        return listOfAnswers
    }

}
