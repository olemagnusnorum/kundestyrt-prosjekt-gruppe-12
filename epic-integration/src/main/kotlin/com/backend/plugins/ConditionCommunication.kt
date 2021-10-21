package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Annotation

class ConditionCommunication(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    private val jsonParser: IParser = ctx.newJsonParser()

    // For demo purposes
    var latestConditionId: String? = "2591225"  // Georges condition

    // Functions for read

    /**
     * Function to get a Condition resource.
     * @param conditionId is the id of the condition to read
     * @return a hapi condition resource
     */
    suspend fun getCondition(conditionId: String): Condition {
        val response: HttpResponse =
            client.get(baseURL + "/Condition/${conditionId}?_format=json") {

            }
        return jsonParser.parseResource(Condition::class.java, response.receive<String>())
    }

    // Functions for search

    /**
     * Function to search for one or more condition resource(s).
     * @param patientId is the id of the patient who has the condition
     * @param outputFormat is either "json" or "xml"
     * @param [code] searchable condition code (for e.g. Pregnancy: 77386006)
     * @return an http response
     */
    suspend fun searchCondition(patientId: String, outputFormat: String, code: String? = null): HttpResponse {
        val response: HttpResponse =
            client.get(baseURL + "/Condition?patient=$patientId&" +
                    (if (code != null) "_include=$code&" else "") +
                    "_format=$outputFormat") {
            }
        return response
    }

    /**
     * Function to search a patient and pregnancy condition.
     * @param conditionId String the id of pregnancy condition
     * @return an http response as a string.
     */
    suspend fun searchPregnantPatient(conditionId: String, outputFormat: String = "json"): String {
        val response: HttpResponse =
            client.get(baseURL + "/Condition?" +
                    "_id=$conditionId&" +
                    "_include=Condition:patient&" +
                    "_format=$outputFormat") {
            }
        return response.receive()
    }

    // Functions for create

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
        condition.setCategory(mutableListOf(
            CodeableConcept(
                Coding(
            "http://terminology.hl7.org/CodeSystem/condition-category",
            "encounter-diagnosis", "Encounter diagnosis")
            )
        ))

        // Set clinical status to active
        condition.setClinicalStatus(
            CodeableConcept(
                Coding(
            "http://terminology.hl7.org/CodeSystem/condition-clinical",
            "active", "Active")
            )
        )

        // Set verification status
        condition.setVerificationStatus(
            CodeableConcept(
                Coding(
            "http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed")
            )
        )

        // Set code to pregnant
        condition.setCode(
            CodeableConcept(
                Coding(
            "urn:oid:2.16.840.1.113883.6.96",
            "77386006", "Pregnant")
            )
        )

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
        condition.setSeverity(
            CodeableConcept(
                Coding(
            "http://hl7.org/fhir/ValueSet/condition-severity",
            "255604002", "Mild")
            )
        )

        val conditionJson = jsonParser.encodeResourceToString(condition)

        // Post the condition to epic
        val response: HttpResponse = client.post("$baseURL/Condition") {

            contentType(ContentType.Application.Json)
            body = conditionJson
        }

        if (response.headers["Location"] != null) {
            latestConditionId = response.headers["Location"]!!.split("/")[5]
        }

        return response
    }

    /**
     * Function to patch note and abatementDate of a condition
     * @param conditionId is a reference to a Condition resource (the id field in a Condition)
     * @param note is a free text comment
     * @param abatementDate is the date the condition ends/ended on the format "YYYY-MM-DD"
     * Guide: https://fhirblog.com/2019/08/13/updating-a-resource-using-patch/
     */
    suspend fun updateCondition(conditionId: String, note: String, abatementDate: String) {
        val conditionPatch = "[{ \"op\": \"replace\", \"path\": \"/note/0\", \"value\": { \"text\": \"$note\" } }," +
                             " { \"op\": \"replace\", \"path\": \"/abatementDateTime\", \"value\": \"$abatementDate\" }]"

        val response: HttpResponse = client.patch("$baseURL/Condition/$conditionId") {
            contentType(ContentType("application", "json-patch+json"))
            body = conditionPatch
        }
    }

    // Functions for parsing

    /**
     * Function to parse a received bundle containing Condition resources
     * to a hapi condition object.
     * @param jsonMessage is the bundle received from a search as a string
     * @return the first condition in the bundle as a hapi condition object
     */
    fun parseConditionBundleStringToObject(jsonMessage: String): Condition? {
        jsonParser.setPrettyPrint(true)

        val bundle = jsonParser.parseResource(Bundle::class.java, jsonMessage)
        if (bundle.total > 0)
            return bundle.entry[0].resource as Condition

        return null
    }
}
