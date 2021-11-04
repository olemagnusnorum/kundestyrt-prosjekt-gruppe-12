package com.backend.plugins.venterbarn

import com.backend.conditionResource
import com.backend.patientResource
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Patient

val navPregnancyMap: MutableMap<String, Condition> = mutableMapOf()

fun Application.venterBarnRoute() {
    // Setup patient, nav and doctor routing
    patientRoute()
    navRoute()
    doctorRoute()

    routing {
        route("/venter-barn") {
            get {
                call.respondTemplate("venterBarn/index.ftl")
            }

            //fhir subscription endpoint for pregnancy subscription
            put("/pregnancy-subscription/{...}"){
                //this endpoint listens for new pregnancy subscriptions and adds the personnummer and abatement time to a map
                val body = call.receive<String>()
                val condition = conditionResource.parse(body)
                val patient = patientResource.read(condition.subject.reference.split("/")[1])

                navPregnancyMap[patient.identifier[0].value] = condition
                call.respond(HttpResponseStatus.CREATED)
            }
        }
    }
}