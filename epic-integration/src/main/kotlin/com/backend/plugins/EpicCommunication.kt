package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.call.*
import io.ktor.http.Parameters
import org.hl7.fhir.r4.model.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*

class EpicCommunication(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir/"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    private val jsonParser: IParser = ctx.newJsonParser()

    // For demo purposes
    var latestPatientId: String = "2591228"
    var latestConditionId: String? = "2591225"  // Georges condition
    var patientCreated: Boolean = false

    /**
     * Finds the patientID of a FHIR Patient object.
     */
    fun getPatientID(patient: Patient) : String {
        val patientURL = patient.id // on the form "https://someaddress.com/theIdWeWant
        return patientURL.substringAfterLast("/")
    }

    /**
     * Searches the database for a Patient with the correct name and birthdate and returns their ID.
     */
    suspend fun getPatientIDFromDatabase(givenName: String, familyName: String, birthdate: String) : String {
        val JSONBundle = patientSearch(givenName, familyName, birthdate)
        val patient : Patient = parseBundleXMLToPatient(JSONBundle, isXML = false)
        val patientID = getPatientID(patient)
        return patientID
    }

    /**
     * Makes an HTTP response request to the epic server at fhir.epic.com
     * Returns an HttpResponse object with a bundle containing up to 1 patient object
     * As default the format returned is JSON (but XML can be returned by setting format to = "xml")
     * Birthdate format yyyy-mm-dd
     */
    suspend fun patientSearch(givenName: String, familyName: String, birthdate: String? = null, identifier: String? = null, outputFormat: String = "json"): String {
        //val token: String = runBlocking { getEpicAccessToken() }
        val response: HttpResponse =
            client.get(baseURL + "/Patient?" +
                    "given=$givenName&" +
                    "family=$familyName&" +
                    (if (birthdate != null) "birthdate=$birthdate&" else "") +
                    (if (identifier != null) "identifier=$identifier&" else "") +
                    "_format=$outputFormat") {
                /*
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }

                 */
            }
        return response.receive()
    }

    /**
     * Makes an HTTP response request to the epic server at fhir.epic.com
     * Returns a HttpResponse object containing the patient resource.
     * As default the format returned is JSON (but XML can be returned by setting format to = "xml")
     * Birthdate format yyyy-mm-dd
     *
     * @property[patientId] the id of the patient resource
     * @property[outputFormat] the requested response format. Either "json" or "xml"
     */
    suspend fun readPatient(patientId: String, outputFormat: String = "json"): HttpResponse {
        val response: HttpResponse =
            client.get(baseURL + "/Patient/" +
                    patientId +
                    "?_format=$outputFormat") {

            }
        return response.receive()
    }

    fun parsePatientStringToObject(jsonMessage: String): Patient {
        val jsonParser: IParser = ctx.newJsonParser()
        jsonParser.setPrettyPrint(true)

        return jsonParser.parseResource(Patient::class.java, jsonMessage)
    }

    /**
     * Function to get a Condition resource.
     */
    suspend fun getCondition(conditionId: String): Condition {
        val response: HttpResponse =
            client.get(baseURL + "/Condition/${conditionId}?_format=json") {

            }
        return jsonParser.parseResource(Condition::class.java, response.receive<String>())
    }

    fun parseBundleXMLToPatient(xmlMessage: String, isXML : Boolean = true ): Patient {
        // Assume we are working with XML
        val parser : IParser = if (isXML) {
            ctx.newXmlParser()
        } else { // If not XML then JSON
            ctx.newJsonParser()
        }
        parser.setPrettyPrint(true)

        val bundle: Bundle = parser.parseResource(Bundle::class.java, xmlMessage)

        val patient: Patient = bundle.entry[0].resource as Patient

        println(patient.name[0].family)

        return patient
    }

    fun parseCommunicationStringToJson(jsonMessage: String): Communication {

        val jsonParser: IParser = ctx.newJsonParser()
        jsonParser.setPrettyPrint(true)

        val communication: Communication = jsonParser.parseResource(Communication::class.java, jsonMessage)

        return communication
    }

    /**
     * Function to create a condition (encounter diagnosis) resource, and save it
     * to epic.
     * @param subject is a reference to a Patient resource (the id field in a Patient)
     * @param note is a free text comment
     * @param onsetDate is the date the condition occurred on the format "YYYY-MM-DD"
     * @param abatementDate is the date the condition ends/ended on the format "YYYY-MM-DD"
     * @return an http response as a string.
     */
    suspend fun createCondition(subject: String, note: String, onsetDate: String, abatementDate: String): HttpResponse {

        val condition = Condition()

        // Set category to encounter-diagnosis
        condition.setCategory(mutableListOf(CodeableConcept(Coding(
            "http://terminology.hl7.org/CodeSystem/condition-category",
            "encounter-diagnosis", "Encounter diagnosis"))))

        // Set clinical status to active
        condition.setClinicalStatus(CodeableConcept(Coding(
            "http://terminology.hl7.org/CodeSystem/condition-clinical",
            "active", "Active")))

        // Set verification status
        condition.setVerificationStatus(CodeableConcept(Coding(
            "http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed")))

        // Set code to pregnant
        condition.setCode(CodeableConcept(Coding(
            "urn:oid:2.16.840.1.113883.6.96",
            "77386006", "Pregnant")))

        // Set a note (optional)
        condition.setNote(mutableListOf(Annotation(MarkdownType(note))))

        // Set subject/patient (Here: Camila Lopez)
        condition.setSubject(Reference("Patient/$subject"))

        // Set onsetPeriod (when the condition began)
        val onset = DateTimeType(onsetDate)
        onset.valueAsString = onsetDate
        condition.setOnset(onset)

        // Set abatement (when the condition ends)
        val abatement = DateTimeType(abatementDate)
        abatement.valueAsString = abatementDate
        condition.setAbatement(abatement)

        // Set severity
        condition.setSeverity(CodeableConcept(Coding(
            "http://hl7.org/fhir/ValueSet/condition-severity",
            "255604002", "Mild")))

        val conditionJson = jsonParser.encodeResourceToString(condition)
        println(conditionJson)

        // Post the condition to epic
        val response: HttpResponse = client.post("$baseURL/Condition") {

            contentType(ContentType.Application.Json)
            body = conditionJson
        }

        if (response.headers["Location"] != null) {
            println(response.headers["Location"])
            latestConditionId = response.headers["Location"]!!.split("/")[5]
        }

        return response
    }

    /**
     * Function to create a patient and save the patient to epics server.
     * In the future, this function should take in parameters, for the
     * different values.
     * @param givenName string
     * @param familyName string
     * @param identifierValue on the format "XXX-XX-XXXX" ("028-27-1234")
     * @return an http response as a string.
     */
    suspend fun createPatient(givenName: String, familyName: String, identifierValue: String): String {
        val patient = Patient()

        // Set birthdate
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val dateInString = "7-Jun-2013"
        val date = formatter.parse(dateInString)
        patient.birthDate = date

        // set gender
        patient.setGender(Enumerations.AdministrativeGender.FEMALE)

        // Set identifier (have not figured out how to give the identifier a value)
        val identifier = Identifier()
        identifier.setValue(identifierValue)
        identifier.setSystem("urn:oid:2.16.840.1.113883.4.1")
        identifier.setUse(Identifier.IdentifierUse.OFFICIAL)
        patient.setIdentifier(mutableListOf(identifier))

        // Set name
        val name = HumanName()
        name.setFamily(familyName)
        name.setGiven(mutableListOf(StringType(givenName)))
        name.setUse(HumanName.NameUse.USUAL)
        patient.setName(mutableListOf(name))

        val patientJson = jsonParser.encodeResourceToString(patient)

        // Post the patient to epic
        val response: HttpResponse = client.post("$baseURL/Patient") {

            contentType(ContentType.Application.Json)
            body = patientJson
        }
        val responseString = response.receive<String>()
        println("HEADERS: ${response.headers}")

        if (response.headers["Location"] != null) {
            println(response.headers["Location"])
            latestPatientId = response.headers["Location"]!!.split("/")[5]
            latestConditionId = null
            patientCreated = true
        }

        return responseString
    }

    suspend fun searchCondition(patientId: String, outputFormat: String): HttpResponse {
        val response: HttpResponse =
            client.get(baseURL + "/Condition?patient=$patientId" +
                    "&category=problem-list-item" +
                    "&_format=$outputFormat") {

            }
        return response
    }

    fun parseConditionBundleStringToObject(jsonMessage: String): Condition? {
        val jsonParser: IParser = ctx.newJsonParser()
        jsonParser.setPrettyPrint(true)

        val bundle = jsonParser.parseResource(Bundle::class.java, jsonMessage)
        if (bundle.total > 0)
            return bundle.entry[0].resource as Condition

        return null
    }

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
        val formatter = ofPattern("yyyy-MM-dd")
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
     * Function to search a patient and pregnancy condition.
     * @param conditionId String the id of pregnancy condition
     * @return a http response as a string.
     */
    suspend fun searchPregnantPatient(conditionId: String, outputFormat: String = "json"): String{
        val response: HttpResponse =
            client.get(baseURL + "/Condition?" +
                    "_id=$conditionId&" +
                    "_include=Condition:patient&" +
                    "_format=$outputFormat") {
            }
        return response.receive()
    }

    suspend fun getQuestionnaire(questionnaireId: String): Questionnaire {
        val response: HttpResponse =
            client.get("$baseURL/Questionnaire/$questionnaireId") {
            }

        return jsonParser.parseResource(Questionnaire::class.java, response.receive<String>())
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
}
