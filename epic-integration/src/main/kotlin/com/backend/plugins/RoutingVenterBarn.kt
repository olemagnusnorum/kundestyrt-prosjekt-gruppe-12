package com.backend.plugins

import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking


fun Application.venterBarnRoute() {
    routing {
        route("/venter-barn") {
            get {
                call.respondTemplate("/venter-barn/index.ftl")
            }

            get("/patient-login") {
                call.respondTemplate("/venter-barn/patient-login.ftl")
            }

            get("/patient") {
                val params = call.receiveParameters()
                val id = params["id"]
                val patient = runBlocking { epicCommunication.parsePatientStringToObject(epicCommunication.readPatient(id!!).receive()) }
                call.respondTemplate("/venter-barn/patient-login.ftl")
            }
        }

    }
}