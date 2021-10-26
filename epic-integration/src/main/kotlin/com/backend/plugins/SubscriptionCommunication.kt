package com.backend.plugins

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

    /**
     * Function to create a subscription resource with channel type rest-hook.
     * @param criteria is the criteria that triggers the subscription. Should be on the form "Resource?searchParam1=value1..."
     * @param endpoint is the relative url of the endpoint that messages should be sent to
     * @param reason is the reason the subscription was created
     * @return the HttpResponse returned by the HAPI server
     */
    suspend fun createSubscription(criteria: String, endpoint: String, reason: String): HttpResponse {

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
    suspend fun createPregnancySubscription(): HttpResponse {
        return createSubscription(
            reason = "Listen for new and updated pregnancy conditions",
            criteria = "Condition?code=77386006",
            endpoint = "venter-barn/pregnancy-subscription"
        )
    }

    /**
     * Function to create a subscription that is triggered by questionnaire resources
     * @return the HttpResponse returned by the HAPI server
     */
    suspend fun createQuestionnaireSubscription(): HttpResponse {
        return createSubscription(
                reason = "Listen for new and updated questionnaires",
                criteria = "Questionnaire?status=active",
                endpoint = "funksjonsvurdering/questionnaire-subscription"
        )
    }

    /**
     * Function to create a subscription that is triggered by questionnaireResponse resources
     * @return the HttpResponse returned by the HAPI server
     */
    suspend fun createQuestionnaireResponseSubscription(): HttpResponse {
        return createSubscription(
                reason = "Listen for new and updated questionnaireResponses",
                criteria = "QuestionnaireResponse?",
                endpoint = "funksjonsvurdering/questionnaireResponse-subscription"
        )
    }

    /**
     * Function to create a subscription resource with channel type rest-hook.
     * @param criteria is the criteria of the subscription resource being searched for
     * @param status is the reason the subscription was created
     * @return the HttpResponse returned by the HAPI server
     */
    suspend fun searchSubscription(criteria: String, reason: String? = null, status: String = "active"): HttpResponse {
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
            searchSubscription(criteria="Condition?code=77386006").receive<String>()
        }

        if(jsonParser.parseResource(Bundle::class.java, pregnancySubscriptionSearch).total > 0) {
            println("Pregnancy subscription already exists")
        } else {
            val response = runBlocking { createPregnancySubscription() }
            println("Pregnancy subscription creation status code: ${response.status}")
        }

        val questionnaireSubscriptionSearch = runBlocking {
            searchSubscription(criteria="Questionnaire?status=active").receive<String>()
        }

        if(jsonParser.parseResource(Bundle::class.java, questionnaireSubscriptionSearch).total > 0) {
            println("Questionnaire subscription already exists")
        } else {
            val response = runBlocking { createQuestionnaireSubscription() }
            println("Questionnaire subscription creation status code: ${response.status}")
        }

        val questionnaireResponseSubscriptionSearch = runBlocking {
            searchSubscription(criteria="QuestionnaireResponse?").receive<String>()
        }

        if(jsonParser.parseResource(Bundle::class.java, questionnaireResponseSubscriptionSearch).total > 0) {
            println("QuestionnaireResponse subscription already exists")
        } else {
            val response = runBlocking { createQuestionnaireResponseSubscription() }
            println("QuestionnaireResponse subscription creation status code: ${response.status}")
        }

        val taskSubscriptionSearch = runBlocking {
            searchSubscription(criteria="Task?").receive<String>()
        }

        if(jsonParser.parseResource(Bundle::class.java, taskSubscriptionSearch).total > 0) {
            println("Task subscription already exists")
        } else {
            val response = runBlocking { createSubscription(
                    reason = "Listen for new and updated tasks",
                    criteria = "Task?",
                    endpoint = "funksjonsvurdering/task-subscription"
            ) }
            println("Task subscription creation status code: ${response.status}")
        }
    }
}