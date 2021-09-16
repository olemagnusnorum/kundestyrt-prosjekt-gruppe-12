package com.backend.plugins

import io.ktor.routing.*
import io.ktor.http.*
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

            //requesting dummy epic serever endpint
            requestEpic()
        }

        //dummy epic server endpoint
        get("/epic") {
            val epicResponse = "THIS IS EPICS RESPONSE"
            call.respondText(epicResponse)
        }

        get("/") {
            call.respondTemplate("index.ftl")
        }

        post("/request-sykepenger") {
            val params = call.receiveParameters()
            print("Received message from frontend!\n")
        }
    }

}
