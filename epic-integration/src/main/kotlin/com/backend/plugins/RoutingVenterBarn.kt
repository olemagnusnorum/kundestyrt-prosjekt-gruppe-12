package com.backend.plugins

import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Patient

val patientResource = PatientResource("local")
val conditionResource = ConditionResource("local")

fun Application.venterBarnRoute(questionnaireResource: QuestionnaireResource) {

    val navPregnancyMap: MutableMap<String, Condition> = mutableMapOf()

    routing {
        route("/venter-barn") {

            get {
                call.respondTemplate("venterBarn/index.ftl")
            }

            get("/patient-login") {
                call.respondTemplate("venterBarn/patient-login.ftl")
            }

            get("/patient") {
                val id = call.parameters["id"]!!

                val patient: Patient? = runBlocking { patientResource.search(identifier = id) }
                // val condition: Condition? = if (patient == null) null else runBlocking {
                //     val responseCondition = conditionCommunication.searchCondition(patient.idElement.idPart, "json", code="77386006").receive<String>()
                //     println(responseCondition)
                //     return@runBlocking conditionCommunication.parseConditionBundleStringToObject(responseCondition)
                // }
                val condition: Condition? = navPregnancyMap[id]
                val note = if (condition == null || condition.note.isEmpty()) null else condition.note[0].text
                val patientName = if (patient == null) "no patient found" else patient.name[0].nameAsSingleString
                val data = mapOf("patient" to patient, "condition" to note, "due_date" to (condition?.abatement.toString().replace("DateTimeType[", "").replace("]", "") ?: "Ingen termindato satt"), "name" to patientName)

                call.respondTemplate("venterBarn/patient.ftl", data)
            }

            get("/doctor") {
                call.respondTemplate("venterBarn/doctor.ftl")
            }

            get("/doctor-form-pregnant") {
                call.respondTemplate("venterBarn/doctor-form-pregnant.ftl")
            }

            get("/doctor-form-pregnant-update") {
                val id = call.parameters["id"]
                val condition: Condition? = if (id == null) null else navPregnancyMap[id]
                    // runBlocking {
                    // val patient = patientCommunication.parseBundleXMLToPatient(patientCommunication.patientSearch(identifier = id), isXML = false)
                    // if (navPregnancyMap.containsKey(id)) {
                    //     return@runBlocking conditionCommunication.parseConditionBundleStringToObject(conditionCommunication.searchCondition(
                    //         patientId = id,
                    //         outputFormat = "json",
                    //         code = "77386006"  // 77386006 is the code for Pregnancy
                    //     ).receive())
                    // }
                    // return@runBlocking null
                // }

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
                println("Inside post")
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
                        conditionResource.updateCondition(conditionId = condition?.idElement!!.idPart, note = note, abatementDate = abatementDate)
                    }
                }

                call.respondRedirect("/venter-barn/doctor")
            }

            //fhir subscription endpoint for pregnancy subscription
            put("/pregnancy-subscription/{...}"){
                //this endpoint listens for new pregnancy subscriptions and adds the personnummer and abatement time to a map
                val body = call.receive<String>()
                val condition = conditionResource.parseConditionsStringToObject(body)

                val patient = patientResource.read(condition.subject.reference.split("/")[1])

                navPregnancyMap[patient.identifier[0].value] = condition

                call.respond(HttpResponseStatus.CREATED)
            }

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