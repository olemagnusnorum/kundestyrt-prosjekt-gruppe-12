package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Subscription

class SubscriptionCommunication(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir/"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    private val jsonParser: IParser = ctx.newJsonParser()

    suspend fun createSubscription(reason: String = "", criteria: String, endpoint: String): HttpResponse {

        val subscription = Subscription()
        subscription.status = Subscription.SubscriptionStatus.ACTIVE
        subscription.reason = reason
        subscription.criteria = criteria

        val subscriptionChannel = Subscription.SubscriptionChannelComponent()
        subscriptionChannel.type = Subscription.SubscriptionChannelType.RESTHOOK
        subscriptionChannel.endpoint = "http://host.docker.internal:8080/$endpoint"
        subscriptionChannel.payload = "application/fhir+json"
        subscription.channel = subscriptionChannel

        val response: HttpResponse = client.post("$baseURL/Subscription"){
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(subscription)
        }

        return response
    }

    suspend fun createPregnancySubscription(): HttpResponse {
        return createSubscription(
            reason = "Listen for new and updated pregnancy conditions",
            criteria = "Condition?code=77386006",
            endpoint = "venter-barn/pregnancy-subscription"
        )
    }

    suspend fun searchSubscription(status: String = "active", criteria: String): HttpResponse {
        return client.get(
            "$baseURL/Subscription?" +
                    "criteria=$criteria&" +
                    "status=$status&" +
                    "_format=json"
        )
    }
}