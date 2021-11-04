package com.backend.plugins.resources

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import com.backend.patientResource
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.hl7.fhir.r4.model.Binary
import org.hl7.fhir.r4.model.Reference
import java.io.File

class BinaryResource(server: String = "public") {

    // The base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val client = HttpClient()
    val jsonParser: IParser = FhirContext.forR4().newJsonParser()

    suspend fun create(binaryFile: File, patientId: String) : HttpResponse {
        val binary = Binary()

        binary.data = binaryFile.readBytes()
        binary.contentType = ContentType.Application.Pdf.toString()
        binary.securityContext = Reference().setReference("Patient/${patientResource.search(identifier = patientId)!!.idElement.idPart}")

        // Post the Binary to the server
        val response: HttpResponse = client.post("$baseURL/Binary"){
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(binary)
        }

        return response
    }
}
