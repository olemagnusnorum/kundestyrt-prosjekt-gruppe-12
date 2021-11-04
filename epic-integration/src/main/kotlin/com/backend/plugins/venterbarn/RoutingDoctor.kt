package com.backend.plugins.venterbarn

import com.backend.conditionResource
import com.backend.patientResource
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition

fun Application.doctorRoute() {
    routing {
        route("/venter-barn") {
            get("/doctor") {
                call.respondTemplate("venterBarn/doctor.ftl")
            }

            get("/doctor-form-pregnant") {
                call.respondTemplate("venterBarn/doctor-form-pregnant.ftl")
            }

            get("/doctor-form-pregnant-update") {
                val id = call.parameters["id"]
                val condition: Condition? = if (id == null) null else navPregnancyMap[id]
                val note = condition?.note?.get(0)?.text
                val abatement = condition?.abatementDateTimeType?.valueAsString
                val data = mapOf("id" to id, "condition" to condition, "abatement" to abatement, "note" to note)
                call.respondTemplate("venterBarn/doctor-form-pregnant-update.ftl", data)
            }

            post("/doctor-form-pregnant") {
                val params = call.receiveParameters()
                val id = params["id"]!!
                val note: String = params["note"]!!
                val onsetDate: String = params["onsetDate"]!!
                val abatementDate: String = params["abatementDate"]!!
                val patient = runBlocking { patientResource.search(identifier = id) }
                if (patient != null) {
                    // Make sure that the patient doesn't already have a registered pregnancy
                    if (!navPregnancyMap.containsKey(id)) {
                        conditionResource.create(patient.idElement.idPart, note, onsetDate, abatementDate)
                    } else {
                        val data = mutableMapOf("error" to "Personen er allerede gravid")
                        call.respondTemplate("venterBarn/doctor-form-pregnant.ftl", data)
                    }
                } else {
                    println("Patient does not exist")
                }

                call.respondRedirect("/venter-barn/doctor")
            }

            post("/doctor-form-pregnant-update") {
                val params = call.receiveParameters()
                val id = params["id"]!!
                val note: String = params["note"]!!
                val abatementDate: String = params["abatementDate"]!!
                val patient = runBlocking { patientResource.search(identifier = id) }
                if (patient != null) {
                    // val condition: Condition? = runBlocking {
                    //     val responseCondition = conditionCommunication.searchCondition(patient.idElement.idPart, "json").receive<String>()
                    //     conditionCommunication.parseConditionBundleStringToObject(responseCondition)
                    // }
                    if (navPregnancyMap.containsKey(id)) {
                        val condition = navPregnancyMap[id]
                        conditionResource.update(conditionId = condition?.idElement!!.idPart, note = note, abatementDate = abatementDate)
                    }
                }

                call.respondRedirect("/venter-barn/doctor")
            }
        }
    }
}
