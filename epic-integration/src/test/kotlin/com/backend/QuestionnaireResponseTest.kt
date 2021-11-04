package com.backend

import com.backend.plugins.resources.PatientResource
import com.backend.plugins.resources.QuestionnaireResource
import com.backend.plugins.resources.QuestionnaireResponseResource
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import kotlin.test.*



// Warning: PER_CLASS Lifecycle means that the same QuestionnaireCommunicationTest class is used for every nested test
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionnaireResponseTest {

    private val patientResource = PatientResource("local")
    private val questionnaireResource = QuestionnaireResource("local")
    private val questionnaireResponseResource = QuestionnaireResponseResource("local")
    private var questionnairesGenerated = false
    private var testPatientID : String = ""

    @BeforeAll
    fun `Create default questionnaires`(){
        // Check if we have questionnaires
        if (questionnairesGenerated) {
            return
        }

        // No predefined questionnaires, so make em
        questionnaireResource.predefinedQuestionnaires = runBlocking { questionnaireResource.readAll() }
        if (questionnaireResource.predefinedQuestionnaires.isEmpty())
            questionnaireResource.createDefaultQuestionnaires()
        questionnairesGenerated = true
    }

    @BeforeAll
    fun `Create default patient`(){
        val patient = runBlocking { patientResource.search(identifier = "04048012345") }

        // If the patient doesn't exist, create it
        if (patient == null) {
            runBlocking {
                patientResource.create("Test", "Pasient", identifierValue = "04048012345",  birthdate = "7-Jun-1990")
            }
        }

        val testPatient = runBlocking { patientResource.search(identifier = "04048012345") }
        testPatientID = testPatient!!.id.split("/")[5]
    }

    @Nested
    inner class createQuestionnaireResponse {
        @Test
        fun `should return a 201 http-response`() {
            val quest: Questionnaire = questionnaireResource.predefinedQuestionnaires[0]
            val response : HttpResponse = runBlocking {
                questionnaireResponseResource.create(quest, mutableListOf("Ja", "Nei", "Ja"), testPatientID)
            }
            assert(response is HttpResponse)
            assert(response.status == HttpStatusCode.Created)
        }
    }

    @Nested
    inner class getQuestionnaireResponse {
        @Test
        fun `should return a QuestionnaireResponse`() {
            // Create a questionnaire response
            val quest: Questionnaire = questionnaireResource.predefinedQuestionnaires[0]
            runBlocking { questionnaireResponseResource.create(quest, mutableListOf("Ja", "Nei", "Ja"), testPatientID) }

            // Get all QuestionnaireResponses
            val responses = runBlocking { questionnaireResponseResource.readAll() }

            // Get the id of the last QuestionnaireResponse
            val qrId = responses.last().idElement.idPart

            // Now get the same QuestionnaireResponse by ID
            val qResponse: QuestionnaireResponse = runBlocking { questionnaireResponseResource.read(qrId)}
            val listOfAnswers = questionnaireResponseResource.retrieveAnswers(qResponse)
            assert(listOfAnswers[0]== "Ja")
            assert(listOfAnswers[1]== "Nei")
            assert(listOfAnswers[2]== "Ja")
        }
    }

    @Nested
    inner class getAllQuestionnaireResponses {
        @Test
        fun `should return more than one resource`() {
            val quest: Questionnaire = questionnaireResource.predefinedQuestionnaires[0]
            runBlocking { questionnaireResponseResource.create(quest, mutableListOf("Ja", "Nei", "Ja"), testPatientID) }
            runBlocking { questionnaireResponseResource.create(quest, mutableListOf("Ja2", "Nei2", "Ja2"), testPatientID) }
            val responses = runBlocking { questionnaireResponseResource.readAll() }

            // Assert that at least 2 QuestionnaireResponses exist
            assert(responses.size > 1)
        }
    }

    @Nested
    inner class getQuestionnaireAnswers {
        @Test
        fun `getQuestionnaireAnswers should return a list of strings`() {
            // Create empty template
            val qr = QuestionnaireResponse()

            // Generate test answers
            val testAnswers = listOf("A", "B", "C")

            // Make an item component for the QuestionnaireResposne
            val item = mutableListOf<QuestionnaireResponse.QuestionnaireResponseItemComponent>()

            // Stuff answers into the item
            for (answer in testAnswers) {
                val component = QuestionnaireResponse.QuestionnaireResponseItemComponent()
                component.answer = mutableListOf(QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent())
                component.answer[0].value = Coding(
                    "some.system",
                    answer, answer
                )
                item.add(component)
            }

            // Put item back into the QuestionnaireResponse resource
            qr.item = item

            // Check if getQuestionnaireAnswers returns the correct answers
            val outputList = questionnaireResponseResource.retrieveAnswers(qr)
            assert(outputList == testAnswers)
        }
    }

    @Nested
    inner class addToInbox {
        @Test
        fun `addToInbox`() {
            // TODO: test inbox
        }
    }

}
