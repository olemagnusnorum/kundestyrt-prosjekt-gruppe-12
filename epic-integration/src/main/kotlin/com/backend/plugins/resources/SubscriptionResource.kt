package com.backend.plugins.resources

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Subscription

class SubscriptionResource(server: String = "public") {

    // The base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val client = HttpClient()
    private val jsonParser: IParser = FhirContext.forR4().newJsonParser()

    /**
     * Function to create a subscription resource with channel type rest-hook.
     * @param [criteria] the criteria that triggers the subscription. Should be on the form "Resource?searchParam1=value1..."
     * @param [endpoint] the relative url of the endpoint that messages should be sent to
     * @param [reason] the reason the subscription was created
     * @return the HttpResponse returned by the HAPI server
     */
    suspend fun create(criteria: String, endpoint: String, reason: String): HttpResponse {
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

    /**
     * Function to create a subscription that is triggered by pregnancy condition resources
     * @return the HttpResponse returned by the HAPI server
     */
    private suspend fun createPregnancySubscription(): HttpResponse {
        return create(
            reason = "Listen for new and updated pregnancy conditions",
            criteria = "Condition?code=77386006",
            endpoint = "venter-barn/pregnancy-subscription"
        )
    }

    /**
     * Function to create a subscription that is triggered by questionnaireResponse resources
     * @return the HttpResponse returned by the HAPI server
     */
    private suspend fun createQuestionnaireResponseSubscription(): HttpResponse {
        return create(
                reason = "Listen for new and updated questionnaireResponses",
                criteria = "QuestionnaireResponse?",
                endpoint = "funksjonsvurdering/questionnaireResponse-subscription"
        )
    }

    /**
     * Function to create a subscription resource with channel type rest-hook.
     * @param [criteria] the criteria of the subscription resource being searched for
     * @param [reason] the reason the subscription was created
     * @param [status] the status of the subscription resource being searched for
     * @return the HttpResponse returned by the HAPI server
     */
    suspend fun search(criteria: String, reason: String? = null, status: String = "active"): HttpResponse {
        return client.get(
            "$baseURL/Subscription?" +
                    "criteria=$criteria&" +
                    "status=$status&" +
                    (if (reason == null) "" else "$reason&") +
                    "_format=json"
        )
    }

    /**
     * Checks if required subscription resources exist in the HAPI server and creates them if necessary
     */
    fun createDefaultSubscriptions() {
        val pregnancySubscriptionSearch = runBlocking {
            search(criteria="Condition?code=77386006").receive<String>()
        }

        if(jsonParser.parseResource(Bundle::class.java, pregnancySubscriptionSearch).total > 0) {
            println("Pregnancy subscription already exists")
        } else {
            val response = runBlocking { createPregnancySubscription() }
            println("Pregnancy subscription creation status code: ${response.status}")
        }

        val questionnaireResponseSubscriptionSearch = runBlocking {
            search(criteria="QuestionnaireResponse?").receive<String>()
        }

        if(jsonParser.parseResource(Bundle::class.java, questionnaireResponseSubscriptionSearch).total > 0) {
            println("QuestionnaireResponse subscription already exists")
        } else {
            val response = runBlocking { createQuestionnaireResponseSubscription() }
            println("QuestionnaireResponse subscription creation status code: ${response.status}")
        }

        val taskSubscriptionSearch = runBlocking {
            search(criteria="Task?").receive<String>()
        }

        if(jsonParser.parseResource(Bundle::class.java, taskSubscriptionSearch).total > 0) {
            println("Task subscription already exists")
        } else {
            val response = runBlocking { create(
                    reason = "Listen for new and updated tasks",
                    criteria = "Task?",
                    endpoint = "funksjonsvurdering/task-subscription"
            ) }
            println("Task subscription creation status code: ${response.status}")
        }
    }
}