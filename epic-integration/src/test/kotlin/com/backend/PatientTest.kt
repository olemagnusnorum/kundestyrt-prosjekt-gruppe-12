package com.backend

import com.backend.plugins.PatientCommunication
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Patient
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.*

// Warning: PER_CLASS Lifecycle means that the same EpicCommunicationTest class is used for every nested test
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatientTest {

    private val patientCommunication = PatientCommunication("local")

    private var patientId = ""

    @Test
    @Order(1)
    fun `createPatient should return a patient resource`() {
        runBlocking {
            patientCommunication.createPatient("Test", "Testest", identifierValue = "123456789",  birthdate = "1-Jan-1990")
            patientId = patientCommunication.latestPatientId
        }

        assert(patientId.isNotEmpty())
    }

    @Test
    @Order(2)
    fun `readPatient should return a patient resource`() {
        println("PatientID: $patientId")
        val patient = runBlocking { patientCommunication.readPatient(patientId) }
        assert(patient.idElement.idPart == patientId)
    }

    @Test
    @Order(3)
    fun `searchPatient should return a string`() {
        val patient = runBlocking {
            val patientResponse = patientCommunication.patientSearch(identifier = "123456789")
            return@runBlocking patientCommunication.parseBundleXMLToPatient(patientResponse, isXML = false)!!
        }

        assert(patient.idElement.idPart.isNotEmpty())
    }

}