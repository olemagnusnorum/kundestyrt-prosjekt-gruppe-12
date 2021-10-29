package com.backend

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import org.junit.jupiter.api.TestInstance
import com.backend.plugins.SubscriptionCommunication
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.request.*
import io.ktor.response.*
import org.junit.jupiter.api.Test
import io.ktor.routing.*
import io.netty.handler.codec.http.HttpResponseStatus
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Bundle
import org.junit.jupiter.api.BeforeAll

// Warning: PER_CLASS Lifecycle means that the same QuestionnaireCommunicationTest class is used for every nested test
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscriptionTest {

    private val sc = SubscriptionCommunication("local")
    private val ctx: FhirContext = FhirContext.forR4()
    private val jsonParser: IParser = ctx.newJsonParser()

    @BeforeAll
    fun `Create subscription we can search for`() {
        runBlocking { sc.createSubscription(
            "Encounter?status=planned",
            "/test",
            "To test createSubscription"
        ) }
    }

    @Test
    fun `createSubscription should return an http response with status value in range 200-299`() {
        val response = runBlocking {sc.createSubscription(
            "Location?status=active",
            "/test",
            "To test createSubscription"
        )}
        assert(response is HttpResponse)
        assertTrue(response.status.value in 200..299)
    }

    @Test
    fun `searchSubscription should return an http response containing at least one subscription`() {
        val response = runBlocking { sc.searchSubscription(
            criteria = "Encounter?status=planned"
        ) }
        assert(response is HttpResponse)
        val responseString = runBlocking { response.receive<String>() }
        val bundle: Bundle = jsonParser.parseResource(Bundle::class.java, responseString)
        assert(bundle.entry.size > 0)
    }

    @Test
    fun `createDeafultSubscriptions should ensure pregnancy condition, questionnaireResponse and task subscription exist`() {

    }
}