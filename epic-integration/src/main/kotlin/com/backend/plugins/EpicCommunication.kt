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
import org.hl7.fhir.r4.model.Annotation
import java.util.Locale
import java.text.SimpleDateFormat
import kotlin.reflect.typeOf

class EpicCommunication {

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    val jsonParser: IParser = ctx.newJsonParser()


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

    /**
     * Function to get a Condition resource.
     */
    suspend fun getCondition(location: String?): Condition {
        val token: String = runBlocking { getEpicAccessToken() }
        val response: HttpResponse =
            client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/${location}?_format=json") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
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
        val token: String = runBlocking { getEpicAccessToken() }

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
        val response: HttpResponse = client.post("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Condition") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            contentType(ContentType.Application.Json)
            body = conditionJson
        }
        val responseString = response.receive<String>()

        println("TOKEN: $token")
        println("RESPONSE HEADER: ${response.headers["Location"]}")
        println("JSON: $conditionJson")

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
        println("HEADERS: ${response.headers}")

        return responseString
    }
}
