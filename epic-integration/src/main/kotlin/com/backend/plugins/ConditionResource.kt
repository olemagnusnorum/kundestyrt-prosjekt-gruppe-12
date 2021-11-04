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

    // The base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val client = HttpClient()
    private val jsonParser: IParser = FhirContext.forR4().newJsonParser()

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
     * Function to create a condition (encounter diagnosis) resource, and save it
     * to epic.
     * @param [subject] a reference to a Patient resource (the id field in a Patient)
     * @param [note] a free text description
     * @param [onsetDate] the date the condition occurred on the format "YYYY-MM-DD"
     * @param [abatementDate] the date the condition ends/ended on the format "YYYY-MM-DD"
     * @return the conditionId of the created condition if successful, else null
     */
    suspend fun create(subject: String, note: String, onsetDate: String, abatementDate: String): String? {
        val condition = Condition()

        // Set category to encounter-diagnosis
        condition.category = mutableListOf(
            CodeableConcept(
                Coding(
                    "http://terminology.hl7.org/CodeSystem/condition-category",
                    "encounter-diagnosis", "Encounter diagnosis"
                )
            )
        )

        // Set clinical status to active
        condition.clinicalStatus = CodeableConcept(
            Coding(
                "http://terminology.hl7.org/CodeSystem/condition-clinical",
                "active", "Active"
            )
        )

        // Set verification status
        condition.verificationStatus = CodeableConcept(
            Coding(
                "http://terminology.hl7.org/CodeSystem/condition-ver-status",
                "confirmed", "Confirmed"
            )
        )

        // Set code to pregnant
        condition.code = CodeableConcept(
            Coding(
                "urn:oid:2.16.840.1.113883.6.96",
                "77386006", "Pregnant"
            )
        )

        // Set a note (optional)
        condition.note = mutableListOf(Annotation(MarkdownType(note)))

        // Set subject/patient (Here: Camila Lopez)
        condition.subject = Reference("Patient/$subject")

        // Set onsetPeriod (when the condition began)
        val onset = DateTimeType(onsetDate)
        onset.valueAsString = onsetDate
        condition.onset = onset

        // Set abatement (when the condition ends)
        val abatement = DateTimeType(abatementDate)
        abatement.valueAsString = abatementDate
        condition.abatement = abatement

        // Set severity
        condition.severity = CodeableConcept(
            Coding(
                "http://hl7.org/fhir/ValueSet/condition-severity",
                "255604002", "Mild"
            )
        )

        // Post the condition to the fhir server
        val response: HttpResponse = client.post("$baseURL/Condition") {
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(condition)
        }

        if (response.headers["Location"] != null) {
            // Find the returned condition id in the header
            return response.headers["Location"]!!.split("/")[5]
        }

        return null
    }

    /**
     * Function to patch (update) note and abatementDate of a condition
     * @param [conditionId] reference to a Condition resource (the id field in a Condition)
     * @param [note] a free text comment
     * @param [abatementDate] the date the condition ends/ended on the format "YYYY-MM-DD"
     */
    suspend fun update(conditionId: String, note: String, abatementDate: String) {
        // Source: https://fhirblog.com/2019/08/13/updating-a-resource-using-patch/
        // TODO : Escape the strings before patching them to the fhir server
        val conditionPatch = "[{ \"op\": \"replace\", \"path\": \"/note/0\", \"value\": { \"text\": \"$note\" } }," +
                             " { \"op\": \"replace\", \"path\": \"/abatementDateTime\", \"value\": \"$abatementDate\" }]"

        client.patch("$baseURL/Condition/$conditionId") {
            contentType(ContentType("application", "json-patch+json"))
            body = conditionPatch
        } as HttpResponse
    }

    /**
     * Parse a json formatted condition-string to a Condition object
     * [conditionJson] the json formatted condition-string
     * @return the parsed Condition object
     */
    fun parse(conditionJson: String): Condition {
        return jsonParser.parseResource(Condition::class.java, conditionJson)
    }
}
