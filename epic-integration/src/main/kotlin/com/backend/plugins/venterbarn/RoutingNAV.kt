package com.backend.plugins.venterbarn

import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.routing.*
import org.hl7.fhir.r4.model.Condition

fun Application.navRoute() {
    routing {
        route ("/venter-barn") {
            get("/nav") {
                val patientMap: MutableMap<String, String> = mutableMapOf()
                navPregnancyMap.forEach { t, u ->
                    patientMap[t] = u.abatement.toString().replace("DateTimeType[", "").replace("]", "")
                }
                val data = mapOf("data" to patientMap)
                call.respondTemplate("venterBarn/nav.ftl", data)
            }
        }
    }
}