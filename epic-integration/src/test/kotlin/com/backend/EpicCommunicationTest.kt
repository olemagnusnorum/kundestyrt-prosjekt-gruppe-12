package com.backend

import com.backend.plugins.EpicCommunication
import org.hl7.fhir.r4.model.Patient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance


// Warning: PER_CLASS Lifecycle means that the same EpicCommunicationTest class is used for every nested test
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EpicCommunicationTest {

    private val epicCommunication = EpicCommunication()

    @Nested
    inner class PatientSearch {
        private lateinit var patientXML: String

        @Test
        suspend fun `Patient_Search should return a string`() {
            patientXML = epicCommunication.patientSearch(given = "Derrick", family = "Lin", birthdate = "1973-06-03")
            assert(patientXML is String)
        }

        @Test
        fun `parseBundleXMLToPatient should parse an xml string to a patient object`() {
            assert(epicCommunication.parseBundleXMLToPatient(patientXML) is Patient)
        }
    }

}