package com.backend

import com.backend.plugins.EpicCommunication
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Patient
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import kotlin.test.*


// Warning: PER_CLASS Lifecycle means that the same EpicCommunicationTest class is used for every nested test
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EpicCommunicationTest {

    private val epicCommunication = EpicCommunication()

    @Nested
    inner class PatientSearch {

        @Test
        fun `Patient_Search should return a string`() {
            assert(runBlocking { epicCommunication.patientSearch(
                givenName = "Derrick",
                familyName = "Lin",
                birthdate = "1973-06-03")
            } is String)
        }

        @Test
        fun `parseBundleXMLToPatient should parse an xml string to a patient object`() {
            val patientXML = runBlocking { epicCommunication.patientSearch(
                givenName = "Derrick",
                familyName = "Lin",
                birthdate = "1973-06-03",
                outputFormat = "xml"
            )}
            assert(patientXML is String)
            assert(epicCommunication.parseBundleXMLToPatient(patientXML) is Patient)
        }

        @Test
        fun `getCondition should return a condition resource for Derric Lin`() {
            val condition = runBlocking {
                epicCommunication.getCondition("Condition/eY-LMUKgFarb5r10D5sXS7nGJO9qELcndS5oncvyDjPHp.lFiCEKE6mt2pIDbyFeBHvU6Z0XikLVgIqkXp8XV1Q3")
            }
            assert(condition is Condition)
            assert(condition.subject.reference == "Patient/eq081-VQEgP8drUUqCWzHfw3")
        }
    }

}