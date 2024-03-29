package com.backend

import com.backend.plugins.resources.ConditionResource
import com.backend.plugins.resources.PatientResource
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.*


// Warning: PER_CLASS Lifecycle means that the same EpicCommunicationTest class is used for every nested test
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConditionTest {

    private val patientResource = PatientResource("local")
    private val conditionResource = ConditionResource("local")

    private var patientId = ""
    private var conditionId = ""

    @Test
    @Order(1)
    fun `createCondition should create and parse a patient, and create and parse a condition`() {
        conditionId = runBlocking {
            val patient = patientResource.search(identifier = "07069012345")
            patientId = patient!!.idElement.idPart
            return@runBlocking conditionResource.create(patientId, "This is a test condition", "2021-10-10", "2022-01-01")!!
        }

        assert(conditionId.isNotEmpty())
    }

    @Test
    @Order(2)
    fun `getCondition should return a condition resource for Kari Nordmann`() {
        val condition = runBlocking { conditionResource.read(conditionId) }
        assert(condition.subject.reference == "Patient/$patientId")
    }

    @Test
    @Order(3)
    fun `searchCondition should find a pregnancy condition resource for Kari Nordmann`() {
        val condition: Condition = runBlocking { conditionResource.search(patientId = patientId)!! }
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
            conditionResource.update(conditionId, note = note, abatementDate = abatementDate)
        }

        val condition = runBlocking { conditionResource.read(conditionId) }
        assert(condition.note[0].text == note)
        assert(condition.abatementDateTimeType?.valueAsString == abatementDate)
    }

}