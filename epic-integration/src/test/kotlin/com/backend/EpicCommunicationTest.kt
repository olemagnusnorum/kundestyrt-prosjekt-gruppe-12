package com.backend

import com.backend.plugins.EpicCommunication
import kotlinx.coroutines.runBlocking
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
            assert(runBlocking { epicCommunication.patientSearch(given = "Derrick", family = "Lin", birthdate = "1973-06-03") } is String)
        }

        @Test
        fun `parseBundleXMLToPatient should parse an xml string to a patient object`() {
            val patientXML = runBlocking { epicCommunication.patientSearch(
                given = "Derrick",
                family = "Lin",
                birthdate = "1973-06-03",
                format = "xml"
            )}
            assert(patientXML is String)
            assert(epicCommunication.parseBundleXMLToPatient(patientXML) is Patient)
        }
    }

}