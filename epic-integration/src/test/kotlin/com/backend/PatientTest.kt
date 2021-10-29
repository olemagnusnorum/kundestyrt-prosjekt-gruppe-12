package com.backend

import com.backend.plugins.PatientResource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.*

// Warning: PER_CLASS Lifecycle means that the same EpicCommunicationTest class is used for every nested test
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatientTest {

    private val patientResource = PatientResource("local")

    private var patientId: String? = ""

    @Test
    @Order(1)
    fun `createPatient should return a patient resource`() {
        patientId = runBlocking { patientResource.create("Test", "Testest", identifierValue = "123456789",  birthdate = "1-Jan-1990") }
        assert(patientId!!.isNotEmpty())
    }

    @Test
    @Order(2)
    fun `readPatient should return a patient resource`() {
        val patient = runBlocking { patientResource.read(patientId!!) }
        assert(patient.idElement.idPart == patientId)
    }

    @Test
    @Order(3)
    fun `searchPatient should return a string`() {
        val patient = runBlocking { patientResource.search(identifier = "123456789") }
        assert(patient!!.idElement.idPart.isNotEmpty())
    }

}
