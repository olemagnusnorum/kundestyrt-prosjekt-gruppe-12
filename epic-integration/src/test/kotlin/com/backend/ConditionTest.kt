package com.backend

import com.backend.plugins.ConditionCommunication
import com.backend.plugins.PatientCommunication
import io.ktor.client.call.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestInstance
import kotlin.test.*


// Warning: PER_CLASS Lifecycle means that the same EpicCommunicationTest class is used for every nested test
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConditionTest {

    private val patientCommunication = PatientCommunication("local")
    private val conditionCommunication = ConditionCommunication("local")

    private var patientId = ""
    private var conditionId = ""

    @Test
    @Order(1)
    fun `createCondition should create and parse a patient, and create and parse a condition`() {
        val conditionResponse = runBlocking {
            val patient = patientCommunication.parseBundleXMLToPatient(patientCommunication.patientSearch(identifier = "07069012345"), isXML = false)
            patientId = patient?.idElement?.idPart!!
            return@runBlocking conditionCommunication.createCondition(patientId, "This is a test condition", "2021-10-10", "2022-01-01")
        }

        conditionId = conditionResponse.headers["Location"]!!.split("/")[5]
        assert(conditionId.isNotEmpty())
    }

    @Test
    @Order(2)
    fun `getCondition should return a condition resource for Kari Nordmann`() {
        val condition = runBlocking { conditionCommunication.getCondition(conditionId) }
        assert(condition.subject.reference == "Patient/$patientId")
    }

    @Test
    @Order(3)
    fun `searchCondition should find a pregnancy condition resource for Kari Nordmann`() {
        val condition: Condition = runBlocking {
            val conditionResponse = conditionCommunication.searchCondition(patientId, outputFormat = "json").receive<String>()
            return@runBlocking conditionCommunication.parseConditionBundleStringToObject(conditionResponse, firstNotLast = false)!!
        }
        assert(condition.idElement.idPart.isNotEmpty())
        assert(condition.code.coding[0].code == "77386006")
        assert(condition.subject.reference == "Patient/$patientId")
    }

    @Test
    @Order(4)
    fun `updateCondition should update the abatement date of a pregnancy condition resource for Kari Nordmann`() {
        val note = "This is an updated test condition"
        val abatementDate = "2022-01-02"
        runBlocking {
            conditionCommunication.updateCondition(conditionId, note = note, abatementDate = abatementDate)
        }

        val condition = runBlocking { conditionCommunication.getCondition(conditionId) }
        assert(condition.note[0].text == note)
        assert(condition.abatementDateTimeType?.valueAsString == abatementDate)
    }

}