package com.backend.plugins.venterbarn

import com.backend.patientResource
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Patient

fun Application.patientRoute() {
    routing {
        route ("/venter-barn") {
            get("/patient-login") {
                call.respondTemplate("venterBarn/patient-login.ftl")
            }

            get("/patient") {
                val id = call.parameters["id"]!!

                val patient: Patient? = runBlocking { patientResource.search(identifier = id) }
                val condition: Condition? = navPregnancyMap[id]
                val note = if (condition == null || condition.note.isEmpty()) null else condition.note[0].text
                val patientName = if (patient == null) "no patient found" else patient.name[0].nameAsSingleString
                val data = mapOf("patient" to patient, "condition" to note, "due_date" to condition?.abatement.toString().replace("DateTimeType[", "").replace("]", ""), "name" to patientName)

                call.respondTemplate("venterBarn/patient.ftl", data)
            }
        }
    }
}