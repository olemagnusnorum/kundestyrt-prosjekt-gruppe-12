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

class EpicCommunication {


    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()

    //this works for getting the xml from the epic server (use hapi fhir to make it a resource?)
    suspend fun patientSearch(given: String, family :String, birthdate : String) :String{
        // birthdate format yyyy-mm-dd
        val token :String =  runBlocking { getEpicAccessToken() }

        val response : HttpResponse = client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Patient?given=$given&family=$family&birthdate=$birthdate&_format=json"){
            headers{
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        val xmlString = response.receive<String>()
        println(xmlString)

        return xmlString
    }

    /**
     * General function to parse a Bundle string to the correct
     * FHIR resource. When using this, one must always typecast
     * the results to be able to get the attributes in the object.
     * @param jsonMessage is a bundle as a String.
     * @return a list containing all resources in the bundle
     * as Resource objects.
     */
    fun parseBundleToResource(jsonMessage: String): MutableList<Resource> {
        val parser: IParser = ctx.newJsonParser()
        parser.setPrettyPrint(true)

        val bundle: Bundle = parser.parseResource(Bundle::class.java, jsonMessage)

        val resources = mutableListOf<Resource>()

        for(entry in bundle.entry) {
            resources.add(entry.resource as Resource)
        }
        return resources
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



