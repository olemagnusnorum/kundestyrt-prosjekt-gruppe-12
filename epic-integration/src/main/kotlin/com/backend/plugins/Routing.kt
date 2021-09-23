package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.response.*
import io.ktor.request.*
import kotlinx.serialization.Serializable


@Serializable
data class Person(val firstName: String?, val lastName: String?, val age: Int?)

fun Application.personRoute() {

    routing {
        get("/person") {
            val person = call.receive<Person>()
            call.respondText("this is a person from json: \n ${person.firstName} ${person.lastName} is ${person.age}")
        }

        //dummy epic server endpoint
        get("/epic") {
            val epicResponse = "Epic"
            call.respondText(epicResponse)
        }

        get("/") {
            call.respondTemplate("index.ftl")
        }

        get("/doctor") {
            call.respondTemplate("doctor.ftl")
        }

        get("/nav-camilla-lopez") {
            call.respondTemplate("nav-camilla-lopez.ftl")
        }

        get("/nav") {
            call.respondTemplate("nav.ftl")
        }

        get("/messages-from-nav") {
            call.respondTemplate("messages-from-nav.ftl")
        }

        get("/messages-sent-from-doctor-confirmation") {
            call.respondTemplate("messages-sent-from-doctor-confirmation.ftl")
        }

        get("/messages-from-doctor") {
            val params = call.receiveParameters()
            call.respondTemplate("messages-from-doctor.ftl")
        }

        post("/request-health-information-confirmation") {
            val params = call.receiveParameters()
            call.respondTemplate("request-health-information-confirmation.ftl")
        }

        post("/request-foreldrepenger") {
            val params = call.receiveParameters()
            print("Received message from frontend!\n")
            call.respondTemplate("nav-camilla-lopez-foreldrepenger.ftl")
        }

        post("/report-child-birth") {
            val params = call.receiveParameters()
            print("Received message from Doctor that Camilla Lopez has had a child!\n")
            call.respondTemplate("doctor-report-child-birth.ftl")
        }

    }

}
