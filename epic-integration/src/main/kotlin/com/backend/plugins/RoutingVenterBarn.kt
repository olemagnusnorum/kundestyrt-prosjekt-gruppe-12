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
                val condition = runBlocking {
                    if (epicCommunication.latestConditionId != null) {
                        epicCommunication.getCondition(epicCommunication.latestConditionId!!)
                    } else {
                        val responseCondition = epicCommunication.searchCondition(id!!, "json").receive<String>()
                        epicCommunication.parseConditionBundleStringToObject(responseCondition)
                    }
                }
                val note = if (condition == null || condition.note.isEmpty()) null else condition.note[0].text
                val data = mapOf("patient" to patient, "condition" to note, "due_date" to (condition?.abatement ?: "Ingen termindato satt"), "name" to patient.name[0].text)

                call.respondTemplate("/venter-barn/patient.ftl", data)
            }
        }

    }
}