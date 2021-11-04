package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Annotation

class ConditionResource(server: String = "public") {

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

    /**
     * Retrieves a Condition from the fhir server
     * @param [conditionId] the id of the condition to read
     * @return a Condition object
     */
    suspend fun read(conditionId: String): Condition {
        if (conditionId.isEmpty())
            throw IllegalArgumentException("Required argument 'conditionId' was empty.")
        val response: HttpResponse = client.get("$baseURL/Condition/${conditionId}?_format=json")
        return jsonParser.parseResource(Condition::class.java, response.receive<String>())
    }

    /**
     * Search for a condition resource
     * @param [patientId] the id of the patient who has the condition
     * @param [code] searchable condition code (for e.g. Pregnancy: 77386006)
     * @param [clinicalStatus] e.g. "active" / "inactive"
     * @param [firstNotLast] whether to return the first or the last condition in the bundle ordered by time of creation
     * @return the first/last registered Condition object that matches the given parameters, else null
     */
    suspend fun search(patientId: String, code: String? = null, clinicalStatus: String? = "active", firstNotLast: Boolean = true): Condition? {
        val response: HttpResponse =
            client.get( "$baseURL/Condition?patient=$patientId&" +
                    (code?.let { "_include=$code&" } ?: "") +
                    (clinicalStatus?.let { "clinical-status=$clinicalStatus" } ?: "") +
                    "_format=json")

        val bundle = jsonParser.parseResource(Bundle::class.java, response.receive<String>())
        if (bundle.total > 0)
            return if (firstNotLast)
                bundle.entry.first().resource as Condition
            else
                bundle.entry.last().resource as Condition

        return null
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

    fun parseConditionsStringToObject(jsonMessage: String): Condition {
        return jsonParser.parseResource(Condition::class.java, jsonMessage)
    }
}
