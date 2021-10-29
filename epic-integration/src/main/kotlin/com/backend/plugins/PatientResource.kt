package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.hl7.fhir.r4.model.*
import java.text.SimpleDateFormat
import java.util.*

class PatientResource(server: String = "public") {

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
    var latestPatientId: String = "2591228"
    var patientCreated: Boolean = false

    /**
     * Makes an HTTP response request to the fhir server
     * @param [patientId] the id of the patient resource
     * @return a Patient object
     */
    suspend fun read(patientId: String): Patient {
        if (patientId.isEmpty())
            throw IllegalArgumentException("Required argument 'patientId' was null.")
        val response: HttpResponse = client.get("$baseURL/Patient/$patientId?_format=json")
        return jsonParser.parseResource(Patient::class.java, response.receive<String>())
    }

    // Functions for search

    /**
     * Makes an HTTP response request to the fhir server containing a patient search
     * @param [givenName] the patient's given name
     * @param [familyName] the patient's surname
     * @param [birthdate] on the format yyyy-mm-dd
     * @param [identifier] is the patient's identifier (e.g. SSN)
     * @return the first registered Patient object that matches the given arguments, else null
     */
    suspend fun search(givenName: String? = null, familyName: String? = null, birthdate: String? = null, identifier: String? = null): Patient? {
        val response: HttpResponse =
            client.get(
                "$baseURL/Patient?" +
                        (givenName?.let { "given=$givenName&" } ?: "") +
                        (familyName?.let { "family=$familyName&" } ?: "") +
                        (birthdate?.let { "birthdate=$birthdate&" } ?: "") +
                        (identifier?.let { "identifier=$identifier&" } ?: "") +
                        "_format=json"
            )

        val bundle: Bundle = jsonParser.parseResource(Bundle::class.java, response.receive<String>())
        if (bundle.entry.size == 0) return null

        // Returns the first registered Patient object in the fhir server that matches the given arguments
        return bundle.entry.first().resource as Patient
    }

    /**
     * Searches the database for a Patient with the correct name and birthdate and returns their ID.
     * @param givenName is the given name of a patient
     * @param familyName is the patient's surname
     * @param birthdate is the patient's birthdate on the format yyyy-mm-dd
     * @return the patients id as a string
     */
    suspend fun getPatientIDFromDatabase(givenName: String, familyName: String, birthdate: String) : String {
        val JSONBundle = search(givenName, familyName, birthdate)
        val patient : Patient = parseBundleXMLToPatient(JSONBundle, isXML = false)!!
        val patientID = getPatientID(patient)
        return patientID
    }

    // Functions for create

    /**
     * Create a patient and post it to the fhir server
     * @param [givenName] given name of the patient
     * @param [familyName] family name of the patient
     * @param [identifierValue] personal identification number of the patient, on the format "ddMMYYxxxxx" (01029912345)
     * @param [birthdate] on the format dd-MMM-yyyy (24-Dec-1999)
     * @param [gender] an AdministrativeGender enum (MALE, FEMALE, OTHER, UNKNOWN, NULL)
     * @return the patientId of the created patient if successful, else null
     */
    suspend fun create(givenName: String, familyName: String, identifierValue: String, birthdate: String, gender: Enumerations.AdministrativeGender = Enumerations.AdministrativeGender.FEMALE): String? {
        val patient = Patient()

        // Set birthdate
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val date = formatter.parse(birthdate)
        patient.birthDate = date

        // Set gender
        patient.gender = gender

        // Set identifier (have not figured out how to give the identifier a value)
        val identifier = Identifier()
        identifier.value = identifierValue
        identifier.system = "urn:oid:2.16.840.1.113883.4.1"
        identifier.use = Identifier.IdentifierUse.OFFICIAL
        patient.identifier = mutableListOf(identifier)

        // Set the patient name
        val name = HumanName()
        name.family = familyName
        name.given = mutableListOf(StringType(givenName))
        name.use = HumanName.NameUse.USUAL
        patient.name = mutableListOf(name)

        // Create a json-encoded string of the patient
        val patientJson = jsonParser.encodeResourceToString(patient)

        // Post the patient to the fhir server
        val response: HttpResponse = client.post("$baseURL/Patient") {
            contentType(ContentType.Application.Json)
            body = patientJson
        }

        if (response.headers["Location"] != null) {
            latestPatientId = response.headers["Location"]!!.split("/")[5]
            conditionResource.latestConditionId = null
            patientCreated = true

            // Return the patientId if a patient was created
            return latestPatientId
        }

        return null
    }

    // Functions for parsing

    /**
     * Function to parse a string on the form of a json patient resource
     * to a hapi Patient object.
     * @param jsonMessage is the patient as a json string
     * @return hapi Patient object
     */
    fun parsePatientStringToObject(jsonMessage: String): Patient {
        val jsonParser: IParser = ctx.newJsonParser()
        jsonParser.setPrettyPrint(true)

        return jsonParser.parseResource(Patient::class.java, jsonMessage)
    }

    /**
     * Function that parses the XML representation of a bundle
     * containing patient resources to a hapi patient object.
     * @param xmlMessage is the bundle received from a search
     * @param isXML is true if the received bundle is in xml format,
     * false if it is json format.
     * @return the first patient in the bundle as a hapi patient object.
     */
    fun parseBundleXMLToPatient(xmlMessage: String, isXML: Boolean = true): Patient? {
        // Assume we are working with XML
        val parser: IParser = if (isXML) {
            ctx.newXmlParser()
        } else { // If not XML then JSON
            ctx.newJsonParser()
        }
        parser.setPrettyPrint(true)

        val bundle: Bundle = parser.parseResource(Bundle::class.java, xmlMessage)
        if (bundle.entry.size == 0) return null

        return bundle.entry[0].resource as Patient
    }

    // Other functions

    /**
     * Finds the patientID of a FHIR Patient object.
     * @param patient is a patient resource
     * @return the patient's id
     */
    fun getPatientID(patient: Patient) : String {
        val patientURL = patient.id // on the form "https://someaddress.com/theIdWeWant
        return patientURL.substringAfterLast("/")
    }
}
