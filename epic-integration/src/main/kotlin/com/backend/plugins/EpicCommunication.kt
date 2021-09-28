package com.backend.plugins

import kotlinx.coroutines.runBlocking

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Communication

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.call.*

class EpicCommunication {


    private val ctx: FhirContext = FhirContext.forR4()

    //this works for getting the xml from the epic server (use hapi fhir to make it a resource?)
    suspend fun patientSearch(given: String, family :String, birthdate : String) :String{
        // birthdate format yyyy-mm-dd
        val token :String =  runBlocking { getEpicAccessToken() }
        val client = HttpClient()

        val response : HttpResponse = client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Patient?given=$given&family=$family&birthdate=$birthdate&_format=json"){
            headers{
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        val xmlString = response.receive<String>()
        println(xmlString)

        return xmlString
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



