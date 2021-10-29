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

    // Functions for read

    /**
     * Makes an HTTP response request to the epic server at fhir.epic.com
     * Returns a Patient object
     * As default the format returned is JSON (but XML can be returned by setting format to = "xml")
     * Birthdate format yyyy-mm-dd
     *
     * @property[patientId] the id of the patient resource
     */
    suspend fun readPatient(patientId: String): Patient {
        val response: HttpResponse =
            client.get(baseURL + "/Patient/" +
                    patientId +
                    "?_format=json") {

            }

        return jsonParser.parseResource(Patient::class.java, response.receive<String>())
    }

    // Functions for search

    /**
     * Makes an HTTP response request to the epic server at fhir.epic.com
     * Returns an HttpResponse object with a bundle containing 0, 1 or more patient object(s)
     * As default the format returned is JSON (but XML can be returned by setting format to = "xml")
     * @param givenName the patient's given name
     * @param familyName the patient's surname
     * @param birthdate on the format yyyy-mm-dd
     * @param identifier is the patient's identifier
     * @param outputFormat is either "json" or "xml"
     * @return an http response as a string
     */
    suspend fun patientSearch(givenName: String? = null, familyName: String? = null, birthdate: String? = null, identifier: String? = null, outputFormat: String = "json"): String {
        //val token: String = runBlocking { getEpicAccessToken() }
        val response: HttpResponse =
            client.get(baseURL + "/Patient?" +
                    (if (givenName != null) "given=$givenName&" else "") +
                    (if (familyName != null) "family=$familyName&" else "") +
                    (if (birthdate != null) "birthdate=$birthdate&" else "") +
                    (if (identifier != null) "identifier=$identifier&" else "") +
                    "_format=$outputFormat") {
            }
        return response.receive()
    }

    /**
     * Searches the database for a Patient with the correct name and birthdate and returns their ID.
     * @param givenName is the given name of a patient
     * @param familyName is the patient's surname
     * @param birthdate is the patient's birthdate on the format yyyy-mm-dd
     * @return the patients id as a string
     */
    suspend fun getPatientIDFromDatabase(givenName: String, familyName: String, birthdate: String) : String {
        val JSONBundle = patientSearch(givenName, familyName, birthdate)
        val patient : Patient = parseBundleXMLToPatient(JSONBundle, isXML = false)!!
        val patientID = getPatientID(patient)
        return patientID
    }

    // Functions for create

    /**
     * Function to create a patient and save the patient to epics server.
     * @param givenName string
     * @param familyName string
     * @param identifierValue on the format "XXX-XX-XXXX" ("028-27-1234")
     * in epics server, not important in hapi server.
     * @return an http response as a string.
     */
    suspend fun createPatient(givenName: String, familyName: String, identifierValue: String, birthdate: String = "7-Jun-2013"): String {
        val patient = Patient()

        // Set birthdate
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val date = formatter.parse(birthdate)
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

        if (response.headers["Location"] != null) {
            latestPatientId = response.headers["Location"]!!.split("/")[5]
            conditionResource.latestConditionId = null
            patientCreated = true
        }

        return responseString
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
