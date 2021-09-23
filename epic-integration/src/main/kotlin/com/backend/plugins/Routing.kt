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

        get("/nav-derrick-lin") {
            call.respondTemplate("nav-derrick-lin.ftl")
        }

        post("/request-sykepenger") {
            val params = call.receiveParameters()
            println("Received request for sykepenger!")
            call.respondTemplate("nav-derrick-lin-sykepenger.ftl")
        }

        post("/create-sykemelding") {
            val params = call.receiveParameters()
            println("Created sykemelding for Derrick Lin!")

            // TODO : Inappropriate blocking method call on the backend
            val response: String = requestEpicPatient("Derrick", "Lin", "1973-06-03")
            val data = mapOf("response" to response)

            call.respondTemplate("doctor-create-sykemelding.ftl", data)
        }

    }

}
