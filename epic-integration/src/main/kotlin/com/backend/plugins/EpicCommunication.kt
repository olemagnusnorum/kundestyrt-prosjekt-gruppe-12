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

    /**
     * Function to get a Condition resource.
     */
    suspend fun getCondition(): String {
        val token: String = runBlocking { getEpicAccessToken() }
        val response: HttpResponse =
            client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Condition?category=encounter-diagnosis&subject=erXuFYUfucBZaryVksYEcMg3") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        return response.receive()
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
     * Function to create a condition (encounter diagnosis) resource, and save it
     * to epic.
     * @param subject is a reference to a Patient resource (the id field in a Patient)
     * @param note is a free text comment
     * @param onsetDate is the date the condition occurred on the format "YYYY-MM-DD"
     * @param abatementDate is the date the condition ends/ended on the format "YYYY-MM-DD"
     * @return an http response as a string.
     */
    suspend fun createCondition(subject: String, note: String, onsetDate: String, abatementDate: String): String {
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
        val onset = DateTimeType()
        onset.valueAsString = onsetDate
        condition.setOnset(onset)

        // Set abatement (when the condition ends)
        val abatement = DateTimeType()
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

        return responseString
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

}
