package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.freemarker.*
import io.ktor.response.*
import io.ktor.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable


@Serializable
data class Person(val firstName: String?, val lastName: String?, val age: Int?)

val epicCommunication = EpicCommunication()

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

        get("/nav") {
            call.respondTemplate("nav.ftl")
        }

        get("/messages-from-nav") {
            call.respondTemplate("messages-from-nav.ftl")
        }

        get("/messages-sent-from-doctor-confirmation") {
            call.respondTemplate("messages-sent-from-doctor-confirmation.ftl")
        }

        post("/messages-from-doctor") {
            val params = call.receiveParameters()
            val response = epicCommunication.parseCommunicationStringToJson("""{"resourceType": "Communication", "id": "eQtjP5dExSGL8QY3jIixZo0TrO52tQfNEGkoWTOJdWCU3", "basedOn": [{"reference": "ServiceRequest/eZykr93PG.4eADHuIA7x31kTgnBtaXdav57aDWVlvDWvi-TiVRQGvTBsmjwpvM8n73"}], "partOf": [{"reference": "Task/ebvg8Qy8tsSAz7oLPJgZXUN3gKXtUQEDEo-3.OI.uuPcHc7JRfVOphJCVs.wEo4DF3"}], "status": "in-progress", "subject": {"reference": "Patient/e5CmvJNKQAN-kUr-XDKfXSQ3", "display": "Patient, Bravo"}, "encounter": {"reference": "Encounter/ePsDBvsehVaICEzX4yNBTGig.9WVSJYHW-td1KddCl1k3"}, "sent": "2021-01-25T06:16:23Z", "recipient": [{"reference": "Organization/eXn64I93.1fbFG3bFDGaXbA3", "display": "Ven B Cbo Transport 5 (Fhir)"}], "sender": {"reference": "Practitioner/ectBdL9yLwfiRop1f5LsU6A3", "display": "Susanna Sammer, MSW"}, "payload": [{"contentString": "Dette er helsedataen du ba om."}]}""")
            val data = mapOf("response" to response.payload[0].content)
            print(data)
            call.respondTemplate("messages-from-doctor.ftl", data)
        }

        post("/request-health-information-confirmation") {
            val params = call.receiveParameters()
            call.respondTemplate("request-health-information-confirmation.ftl")
        }

        post("/request-sykepenger") {
            val params = call.receiveParameters()
            println("Received request for sykepenger!")

            // TODO : Inappropriate blocking method call on the backend
            val response: String = epicCommunication.patientSearch("Derrick", "Lin", "1973-06-03")
            val data = mapOf("response" to response)

            call.respondTemplate("nav-derrick-lin-sykepenger.ftl", data)
        }

        post("/create-sykemelding") {
            val params = call.receiveParameters()
            println("Created sykemelding for Derrick Lin!")

            // TODO : Inappropriate blocking method call on the backend
            val response: String = epicCommunication.patientSearch("Derrick", "Lin", "1973-06-03")
            val data = mapOf("response" to response)

            call.respondTemplate("doctor-create-sykemelding.ftl", data)
        }

        get("/patient") {
            val patientId = epicCommunication.latestPatientId
            val responseCondition = runBlocking { epicCommunication.searchCondition(patientId, "json").receive<String>() }
            val responsePatient = runBlocking { epicCommunication.readPatient(patientId, "json").receive<String>() }
            val condition = epicCommunication.parseConditionBundleStringToObject(responseCondition)
            val patient = epicCommunication.parsePatientStringToObject(responsePatient)

            val data = mapOf("condition" to condition, "due_date" to (condition?.abatement ?: "Ingen termindato satt"), "name" to patient.name[0].text)
            call.respondTemplate("patient.ftl", data)
        }

        post("/create-patient") {
            val params = call.receiveParameters()

            val given : String = params["given"]!!
            val family : String = params["family"]!!
            val identifierValue : String = params["identifierValue"]!!

            runBlocking { epicCommunication.createPatient(given, family, identifierValue) }

            val data = mapOf("response" to family)
            call.respondTemplate("create-patient-confirmation.ftl", data)
        }
    }

}
