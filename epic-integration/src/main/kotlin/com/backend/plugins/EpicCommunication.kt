package com.backend.plugins

import kotlinx.coroutines.runBlocking

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.rest.api.MethodOutcome
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.call.*
import org.hl7.fhir.r4.model.*
import java.util.Locale
import java.text.SimpleDateFormat
import kotlin.reflect.typeOf

class EpicCommunication {

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    private val jsonParser: IParser = ctx.newJsonParser()

    // For demo purposes
    var latestPatientId: String = "eq081-VQEgP8drUUqCWzHfw3"
    var latestConditionId: String? = "eVGf2YljIMIk76IcfbNpjWQ3"  // Derrick Lin condition


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
     * Returns a patient object on String format.
     * As default the format returned is JSON (but XML can be returned by setting format to = "xml")
     * Birthdate format yyyy-mm-dd
     */
    suspend fun patientSearch(givenName: String, familyName: String, birthdate: String? = null, identifier: String? = null, outputFormat: String = "json"): String {
        val token: String = runBlocking { getEpicAccessToken() }
        val response: HttpResponse =
            client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Patient?" +
                    "given=$givenName&" +
                    "family=$familyName&" +
                    (if (birthdate != null) "birthdate=$identifier&" else "") +
                    (if (identifier != null) "identifier=$identifier&" else "") +
                    "_format=$outputFormat") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        return response.receive()
    }

    suspend fun readPatient(patientId: String, outputFormat: String = "json"): HttpResponse {
        val token: String = runBlocking { getEpicAccessToken() }
        val response: HttpResponse =
            client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Patient/" +
                    patientId +
                    "?_format=$outputFormat") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        return response.receive()
    }

    fun parsePatientStringToObject(jsonMessage: String): Patient {
        val jsonParser: IParser = ctx.newJsonParser()
        jsonParser.setPrettyPrint(true)

        return jsonParser.parseResource(Patient::class.java, jsonMessage)
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
     * Function to create a patient and save the patient to epics server.
     * In the future, this function should take in parameters, for the
     * different values.
     * @param givenName string
     * @param familyName string
     * @param identifierValue on the format "XXX-XX-XXXX" ("028-27-1234")
     * @return an http response as a string.
     */
    suspend fun createPatient(givenName: String, familyName: String, identifierValue: String): String {
        val token: String = runBlocking { getEpicAccessToken() }
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
        val response: HttpResponse = client.post("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Patient") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            contentType(ContentType.Application.Json)
            body = patientJson
        }
        val responseString = response.receive<String>()

        if (response.headers["Location"] != null) {
            latestPatientId = response.headers["Location"]!!.split("/")[1]
        }

        return responseString
    }

    suspend fun searchCondition(patientId: String, outputFormat: String): HttpResponse {
        val token: String = runBlocking { getEpicAccessToken() }
        val response: HttpResponse =
            client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Condition?patient=$patientId" +
                    "&category=problem-list-item" +
                    "&_format=$outputFormat") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
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



}


//Maybe TODO: Find more general parsing. Ex.: From Bundle to whatever object is in it.
/**
 * Parse a bundle xml to Patient object using Hapi Parser.
 * Intended to receive XML from requestEpicPatient()
 *
 * The hapi context object is used to create a new XML parser
 * instance. The parser can then be used to parse (or unmarshall) the
 * string message into a Patient object
 */



