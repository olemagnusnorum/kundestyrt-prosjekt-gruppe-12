package com.backend.plugins

import kotlinx.coroutines.runBlocking


import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.call.*



//this works for getting the xml from the epic server (use hapi fhir to make it a resource?)
suspend fun requestEpicPatient(given: String, family :String, birthdate : String) :String {

    // birthdate format yyyy-mm-dd
    val token :String =  runBlocking { getEpicAccessToken() }
    val client = HttpClient()

    val response : HttpResponse = client.get("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4/Patient?given=$given&family=$family&birthdate=$birthdate"){
        headers{
            append(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    val xmlString = response.receive<String>()
    println(xmlString)

    return xmlString


}


