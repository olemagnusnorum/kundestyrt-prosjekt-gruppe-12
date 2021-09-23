package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.Patient
import java.net.URL
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun requestEpic() : String {

    val serverURLString: String = "https://fhir.epic.com/interconnect-fhir-oauth/"
    val url = URL(serverURLString)

    val ctx = FhirContext.forR4()

    // Create a client
    val client = ctx.newRestfulGenericClient(serverURLString)

    val token = GlobalScope.launch { getEpicAccessToken() }.toString()

    val p = client
        .read()
        .resource(Patient::class.java)
        .withId(123L)
        .withAdditionalHeader("Bearer", token)
        .execute()

    //val patient = client.read().resource(Patient::class.java).withId("123").execute()

    //println(patient)

    return p.toString()

//    with(url.openConnection() as HttpURLConnection){
//        requestMethod = "GET"
//        println("THIS IS THE RESPONSE from $serverURLString")
//        println("Response code $responseCode")
//
//        inputStream.bufferedReader().use {
//            it.lines().forEach{ line ->
//                println(line)
//            }
//        }
//    }
}

