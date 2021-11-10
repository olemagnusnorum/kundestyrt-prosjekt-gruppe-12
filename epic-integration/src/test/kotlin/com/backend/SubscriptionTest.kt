package com.backend

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import org.junit.jupiter.api.TestInstance
import com.backend.plugins.resources.SubscriptionResource
import io.ktor.client.call.*
import io.ktor.client.statement.*
import org.junit.jupiter.api.Test
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Bundle
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested

// Warning: PER_CLASS Lifecycle means that the same SubscriptionTest class is used for every nested test
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscriptionTest {

    private val subscriptionResource = SubscriptionResource("local")
    private val ctx: FhirContext = FhirContext.forR4()
    private val jsonParser: IParser = ctx.newJsonParser()

    @BeforeAll
    fun `Create subscription we can search for`() {
        runBlocking {
            subscriptionResource.create(
            "Encounter?status=planned",
            "/test",
            "To test createSubscription"
            )
        }
    }

    @Nested
    inner class CreateSubscription {
        private val response: HttpResponse = runBlocking {
            subscriptionResource.create(
                "Location?status=active",
                "/test",
                "To test createSubscription"
            )
        }
        @Test
        fun `the http response should have a status value in range 200-299`() {
            assertTrue(response.status.value in 200..299)
        }
    }

    @Test
    fun `searchSubscription should return an http response containing at least one subscription`() {
        val response: HttpResponse = runBlocking {
            subscriptionResource.search(
                criteria = "Encounter?status=planned"
            )
        }
        val responseString = runBlocking { response.receive<String>() }
        val bundle: Bundle = jsonParser.parseResource(Bundle::class.java, responseString)
        assert(bundle.entry.size > 0)
    }
}