package com.backend.plugins

import kotlinx.coroutines.runBlocking

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.call.*
import org.hl7.fhir.r4.model.*
import java.util.Locale
import java.text.SimpleDateFormat

class EpicCommunication {

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    val jsonParser: IParser = ctx.newJsonParser()

    /**
     * Makes an HTTP response request to the epic server at fhir.epic.com
     * Returns a patient object on String format.
     * As default the format returned is JSON (but XML can be returned by setting format to = "xml")
     * Birthdate format yyyy-mm-dd
     */
    suspend fun patientSearch(givenName: String, familyName: String, birthdate: String, outputFormat: String = "json"): String {
        val token: String = runBlocking { getEpicAccessToken() }
        val response: HttpResponse =
            client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Patient?" +
                    "given=$givenName&" +
                    "family=$familyName&" +
                    "birthdate=$birthdate&" +
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

    fun parseBundleXMLToPatient(xmlMessage: String): Patient {


        val parser: IParser = ctx.newXmlParser()
        parser.setPrettyPrint(true)

        val jsonParser: IParser = ctx.newJsonParser() //made
        jsonParser.setPrettyPrint(true) //made

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
     * @return an http response as a string.
     */
    suspend fun createPatient(): String {
        val token: String = runBlocking { getEpicAccessToken() }
        val patient = Patient()

        // Set date
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val dateInString = "7-Jun-2013"
        val date = formatter.parse(dateInString)
        patient.birthDate = date

        // set gender
        patient.setGender(Enumerations.AdministrativeGender.FEMALE)

        // Set identifier (have not figured out how to give the identifier a value)
        val identifier = Identifier()
        identifier.setValue("028-27-1234")
        identifier.setSystem("urn:oid:2.16.840.1.113883.4.1")
        identifier.setUse(Identifier.IdentifierUse.OFFICIAL)
        patient.setIdentifier(mutableListOf(identifier))

        // Set name
        val name = HumanName()
        name.setFamily("Nordmann")
        name.setGiven(mutableListOf(StringType("Kari")))
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

    fun parseConditionBundleStringToObject(jsonMessage: String): Condition {
        val jsonParser: IParser = ctx.newJsonParser()
        jsonParser.setPrettyPrint(true)

        val bundle = jsonParser.parseResource(Bundle::class.java, jsonMessage)
        val condition = bundle.entry[0].resource

        return condition as Condition
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



