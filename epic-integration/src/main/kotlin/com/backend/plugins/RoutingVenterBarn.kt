package com.backend.plugins

import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Patient
import java.time.format.DateTimeFormatter


fun Application.venterBarnRoute() {

    val questionnaireCommunication = QuestionnaireCommunication("local")


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

                val patient: Patient? = runBlocking {
                    val patientResponse = patientCommunication.patientSearch(identifier = id)
                    return@runBlocking patientCommunication.parseBundleXMLToPatient(patientResponse, isXML = false)
                }
                val condition: Condition? = if (patient == null) null else runBlocking {
                    val responseCondition = conditionCommunication.searchCondition(patient.idElement.idPart, "json").receive<String>()
                    println(responseCondition)
                    return@runBlocking conditionCommunication.parseConditionBundleStringToObject(responseCondition)
                }
                val note = if (condition == null || condition.note.isEmpty()) null else condition.note[0].text
                val patientName = if (patient == null) "no patient found" else patient.name[0].nameAsSingleString
                val data = mapOf("patient" to patient, "condition" to note, "due_date" to (condition?.abatement ?: "Ingen termindato satt"), "name" to patientName)

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
                val condition: Condition? = if (id == null) null else runBlocking {
                    val patient = patientCommunication.parseBundleXMLToPatient(patientCommunication.patientSearch(identifier = id), isXML = false)
                    if (patient != null) {
                        return@runBlocking conditionCommunication.parseConditionBundleStringToObject(conditionCommunication.searchCondition(
                            patientId = patient.idElement.idPart,
                            outputFormat = "json",
                            code = "77386006"  // 77386006 is the code for Pregnancy
                        ).receive())
                    }
                    return@runBlocking null
                }

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
                val patient = runBlocking { patientCommunication.parseBundleXMLToPatient(patientCommunication.patientSearch(identifier = id), isXML = false) }
                if (patient != null) {
                    val condition: Condition? = runBlocking {
                        val responseCondition = conditionCommunication.searchCondition(patient.idElement.idPart, "json").receive<String>()
                        conditionCommunication.parseConditionBundleStringToObject(responseCondition)
                    }

                    // Make sure that the patient doesn't already have a registered pregnancy
                    if (condition == null) {
                        conditionCommunication.createCondition(patient.idElement.idPart, note, onsetDate, abatementDate)
                    }
                }

                call.respondRedirect("/venter-barn/doctor")
            }

            post("/doctor-form-pregnant-update") {
                val params = call.receiveParameters()
                val id = params["id"]!!
                val note: String = params["note"]!!
                val abatementDate: String = params["abatementDate"]!!
                val patient = runBlocking { patientCommunication.parseBundleXMLToPatient(patientCommunication.patientSearch(identifier = id), isXML = false) }
                if (patient != null) {
                    val condition: Condition? = runBlocking {
                        val responseCondition = conditionCommunication.searchCondition(patient.idElement.idPart, "json").receive<String>()
                        conditionCommunication.parseConditionBundleStringToObject(responseCondition)
                    }

                    // Make sure that the condition exists
                    if (condition != null) {
                        conditionCommunication.updateCondition(conditionId = condition.idElement.idPart, note = note, abatementDate = abatementDate)
                    }
                }

                call.respondRedirect("/venter-barn/doctor")
            }

            //fhir subscription endpoint for pregnancy subscription
            put("/pregnancy-subscription/{...}"){
                //this function should check if it is an update on pregnancy or a new pregnancy condition
                val body = call.receive<String>()
                println("message received")
                println(body)
                call.respond(HttpResponseStatus.CREATED)
            }
            ///
            // OLD
            ///

            get("/person") {
                val person = call.receive<Person>()
                call.respondText("this is a person from json: \n ${person.firstName} ${person.lastName} is ${person.age}")
            }

            //dummy epic server endpoint
            get("/epic") {
                val epicResponse = "Epic"
                call.respondText(epicResponse)
            }

            get("/nav-derrick-lin") {
                call.respondTemplate("venterBarn/nav-derrick-lin.ftl")
            }

            get("/nav") {
                call.respondTemplate("venterBarn/nav.ftl")
            }

            get("/messages-from-nav") {
                call.respondTemplate("venterBarn/messages-from-nav.ftl")
            }

            get("/messages-sent-from-doctor-confirmation") {
                call.respondTemplate("venterBarn/messages-sent-from-doctor-confirmation.ftl")
            }

            post("/messages-from-doctor") {
                val params = call.receiveParameters()
                //val response = epicCommunication.parseCommunicationStringToJson("""{"resourceType": "Communication", "id": "eQtjP5dExSGL8QY3jIixZo0TrO52tQfNEGkoWTOJdWCU3", "basedOn": [{"reference": "ServiceRequest/eZykr93PG.4eADHuIA7x31kTgnBtaXdav57aDWVlvDWvi-TiVRQGvTBsmjwpvM8n73"}], "partOf": [{"reference": "Task/ebvg8Qy8tsSAz7oLPJgZXUN3gKXtUQEDEo-3.OI.uuPcHc7JRfVOphJCVs.wEo4DF3"}], "status": "in-progress", "subject": {"reference": "Patient/e5CmvJNKQAN-kUr-XDKfXSQ3", "display": "Patient, Bravo"}, "encounter": {"reference": "Encounter/ePsDBvsehVaICEzX4yNBTGig.9WVSJYHW-td1KddCl1k3"}, "sent": "2021-01-25T06:16:23Z", "recipient": [{"reference": "Organization/eXn64I93.1fbFG3bFDGaXbA3", "display": "Ven B Cbo Transport 5 (Fhir)"}], "sender": {"reference": "Practitioner/ectBdL9yLwfiRop1f5LsU6A3", "display": "Susanna Sammer, MSW"}, "payload": [{"contentString": "Dette er helsedataen du ba om."}]}""")
                //val data = mapOf("response" to response.payload[0].content)
                // Should not be using communication
                val data = mapOf("response" to "En streng")
                print(data)
                call.respondTemplate("venterBarn/messages-from-doctor.ftl", data)
            }

            post("/request-health-information-confirmation") {
                val params = call.receiveParameters()
                call.respondTemplate("venterBarn/request-health-information-confirmation.ftl")
            }

            post("/request-sykepenger") {
                val params = call.receiveParameters()
                println("Received request for sykepenger!")

                val response: String = patientCommunication.patientSearch("Derrick", "Lin", "1973-06-03")
                val data = mapOf("response" to response)

                call.respondTemplate("venterBarn/nav-derrick-lin-sykepenger.ftl", data)
            }

            post("/create-sykemelding") {
                val params = call.receiveParameters()
                println("Created sykemelding for Derrick Lin!")

                val response: String = patientCommunication.patientSearch("Derrick", "Lin", "1973-06-03")
                val data = mapOf("response" to response)

                call.respondTemplate("venterBarn/doctor-create-sykemelding.ftl", data)
            }

            post("/create-patient") {
                val params = call.receiveParameters()

                val given : String = params["given"]!!
                val family : String = params["family"]!!
                val identifierValue : String = params["identifierValue"]!!

                runBlocking { patientCommunication.createPatient(given, family, identifierValue) }

                val data = mapOf("response" to family)
                call.respondTemplate("venterBarn/create-patient-confirmation.ftl", data)
            }

            post("/create-pregnancy") {
                val response = runBlocking { conditionCommunication.createCondition(
                    patientCommunication.latestPatientId, "The patient is pregnant.",
                    "2015-01-01", "2015-08-30") }

                val conditionId = response.headers["Location"]!!.split("/")[5]
                navInbox.addToInbox("Pregnancy", conditionId)
                call.respondRedirect("/venter-barn/doctor")
            }

            //new questionnaire site
            post("/create-questionnaire"){
                val params = call.receiveParameters()

                val question1 = params["question1"]!!
                val question2 = params["question2"]!!


                val jsonResponse = runBlocking { questionnaireCommunication.createQuestionnaire(params) }
                val data = mapOf("response" to jsonResponse)
                //testing inbox function

                navInbox.addToInbox("Questionnaire", jsonResponse)
                call.respondTemplate("venterBarn/create-questionnaire-confirmation.ftl", data)
            }

            //new questionnaire site
            get("/questionnaire"){
                call.respondTemplate("venterBarn/questionnaire.ftl")
            }

            //new inbox site for nav
            get("/nav-inbox"){
                call.respondTemplate("venterBarn/nav-inbox.ftl", navInbox.getInbox())
            }

            //new inbox site for doctor
            get("/doctor-inbox"){
                call.respondTemplate("venterBarn/nav-inbox.ftl", doctorInbox.getInbox())
            }

            //get pregnancy information
            get("/nav-inbox/pregnancy/{id}"){
                val conditionId: String = call.parameters["id"]!!
                val response = runBlocking { conditionCommunication.searchPregnantPatient(conditionId) }
                val data = mapOf("response" to response)
                call.respondTemplate("venterBarn/show-info.ftl", data)
            }
        }

    }
}