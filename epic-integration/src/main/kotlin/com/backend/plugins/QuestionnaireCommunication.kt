package com.backend.plugins
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.util.BundleBuilder
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class QuestionnaireCommunication(server: String = "public") {

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
    var inbox: MutableMap<String, MutableList<Questionnaire>> = mutableMapOf()

    var predefinedQuestionnaires = mutableListOf<Questionnaire>()

    fun createDefaultQuestionnaires() {
        var questions = Parameters.build {
            append("question1", "Kan pasienten høre?")
            append("question2", "Kan pasienten se?")
            append("question3", "Kan pasienten prate?")
        }

        runBlocking { predefinedQuestionnaires.add(getQuestionnaire(createQuestionnaire(questions))) }

        questions = Parameters.build {
            append("question1", "Kan pasienten ligge?")
            append("question2", "Kan pasienten stå?")
            append("question3", "Kan pasienten gå?")
        }

        runBlocking { predefinedQuestionnaires.add(getQuestionnaire(createQuestionnaire(questions))) }

        questions = Parameters.build {
            append("question1", "Kan pasienten spille trompet?")
            append("question2", "Kan pasienten løse kryssord?")
            append("question3", "Kan pasienten danse cancan?")
        }

        runBlocking { predefinedQuestionnaires.add(getQuestionnaire(createQuestionnaire(questions))) }
    }

    /**
     * Function to create a questionnaire and save the questionnaire to fhir server.
     * In the future, this function should take in parameters, for the
     * different values.
     * @param questions Params of the questions. Get these from navigation. When you click
     * the "Register questionnaire" button you receive the params to send in.
     * @return id of the created questionnaire or "EMPTY" if a questionnaire was not created.
     */
    suspend fun createQuestionnaire(questions: Parameters, patientId: String = "2559067"): String{

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
            if (t == "patientId") {
                null
            }
            else {
                number++
                var item = Questionnaire.QuestionnaireItemComponent()
                item.setLinkId(number.toString())
                item.setText(question[0])
                item.setType(Questionnaire.QuestionnaireItemType.STRING)
                items.add(item)
            }
        }

        questionnaire.setItem(items)

        // Set the identifier. Should be on the format UUID/patientID.
        // This allows us to connect a questionnaire to a patient.
        val identifier = Identifier()
        val uuid = UUID.randomUUID().toString()
        identifier.setValue("$uuid/$patientId")
        questionnaire.setIdentifier(mutableListOf(identifier))

        val questionnaireJson = jsonParser.encodeResourceToString(questionnaire)


        //post the questionnaire to the server
        val response: HttpResponse = client.post("$baseURL/Questionnaire"){

            contentType(ContentType.Application.Json)
            body = questionnaireJson
        }

        val responseString = response.receive<String>()

        if (response.headers["Location"] != null) {
            var responseId = response.headers["Location"]!!.split("/")[5]

            return responseId
        }

        return "EMPTY"
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
     * Add questionnaire to local inbox when it arrives via subscription
     */
    fun addToInbox(json: String) {
        val questionnaire = jsonParser.parseResource(Questionnaire::class.java, json)
        val patientId = questionnaire.identifier[0].value.substringAfter("/")

        if (inbox.containsKey(patientId)) {
            inbox[patientId]?.add(questionnaire)
        }
        else {
            var newList = mutableListOf<Questionnaire>(questionnaire)
            inbox[patientId] = newList
        }
    }

    /**
     * Search after questionnaires.
     */
    suspend fun searchQuestionnaires() : Bundle {
        val response: HttpResponse =
                client.get("$baseURL/Questionnaire?_format=json") {
                }
        return jsonParser.parseResource(Bundle::class.java, response.receive<String>())
    }
}
