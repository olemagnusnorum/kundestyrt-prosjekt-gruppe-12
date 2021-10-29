package com.backend

import com.backend.plugins.PatientCommunication
import com.backend.plugins.QuestionnaireCommunication
import com.backend.plugins.QuestionnaireResponseCommunication
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

    private val qrc = QuestionnaireResponseCommunication("local")
    private val patientCommunication = PatientCommunication("local")
    private val questionnaireCommunication = QuestionnaireCommunication("local")
    private var questionnairesGenerated = false
    private var testPatientID : String = ""



    @BeforeAll
    fun `Create default questionnaires`(){
        // Check if we have questionnaires
        if (questionnairesGenerated) {
            return
        }

        // No predefined questionnaires, so make em
        val questionnaireBundle = runBlocking { questionnaireCommunication.searchQuestionnaires() }

        if (questionnaireBundle.entry.size >= 3) {
            for (bundleComponent in questionnaireBundle.entry) {
                val questionnaire = bundleComponent.resource as Questionnaire
                questionnaireCommunication.predefinedQuestionnaires.add(questionnaire)
            }
        }
        else {
            questionnaireCommunication.createDefaultQuestionnaires()
        }
        questionnairesGenerated = true

    }

    @BeforeAll
    fun `Create default patient`(){
        val patient = runBlocking {
            val response = patientCommunication.patientSearch(identifier = "04048012345")
            patientCommunication.parseBundleXMLToPatient(response, isXML = false)
        }

        // If the patient doesn't exist, create it
        if (patient == null) {
            runBlocking {
                patientCommunication.createPatient("Test", "Pasient", identifierValue = "04048012345",  birthdate = "7-Jun-1990")
            }
        }

        val testPatient = runBlocking {
            val response = patientCommunication.patientSearch(identifier = "04048012345")
            patientCommunication.parseBundleXMLToPatient(response, isXML = false)
        }
        testPatientID = testPatient!!.id.split("/")[5]
    }

    @Nested
    inner class createQuestionnaireResponse {
        @Test
        fun `should return a 201 http-response`() {
            val quest: Questionnaire = questionnaireCommunication.predefinedQuestionnaires[0]
            val response : HttpResponse = runBlocking {
                qrc.createQuestionnaireResponse(quest, mutableListOf("Ja", "Nei", "Ja"), testPatientID)
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
            val quest: Questionnaire = questionnaireCommunication.predefinedQuestionnaires[0]
            val response : HttpResponse = runBlocking {
                qrc.createQuestionnaireResponse(quest, mutableListOf("Ja", "Nei", "Ja"), testPatientID)
            }

            // Get all Questionnaireresponses
            val qrBundle = runBlocking { qrc.getAllQuestionnaireResponses() }

            // Get the id of the last QuestionnaireResponse
            val lastQR = qrBundle.entry.last()
            val qrId = lastQR.fullUrl.split("/")[5]

            // Now get the same QuestionnaireResponse by ID
            val qResponse = runBlocking { qrc.getQuestionnaireResponse(qrId)}

            assert(qResponse is QuestionnaireResponse)
            val listOfAnswers = qrc.getQuestionnaireAnswers(qResponse)
            assert(listOfAnswers[0]== "Ja")
            assert(listOfAnswers[1]== "Nei")
            assert(listOfAnswers[2]== "Ja")
        }
    }

    @Nested
    inner class getAllQuestionnaireResponses {
        @Test
        fun `should return more than one resource`() {
            val quest: Questionnaire = questionnaireCommunication.predefinedQuestionnaires[0]
            val response1 : HttpResponse = runBlocking {
                qrc.createQuestionnaireResponse(quest, mutableListOf("Ja", "Nei", "Ja"), testPatientID)
            }
            val response2 : HttpResponse = runBlocking {
                qrc.createQuestionnaireResponse(quest, mutableListOf("Ja2", "Nei2", "Ja2"), testPatientID)
            }
            val qrBundle = runBlocking { qrc.getAllQuestionnaireResponses() }
            // Assert that at least 2 QuestionnaireResponses exist
            assert(qrBundle.total > 1)
        }
    }

    @Nested
    inner class getQuestionnaireAnswers {
        @Test
        fun `getQuestionnaireAnswers should return a list of strings`() {
            // Create empty template
            val qr = QuestionnaireResponse()

            // Generate test answers
            val testAnswers = listOf<String>("A", "B", "C")

            // Make an item component for the QuestionnaireResposne
            val item = mutableListOf<QuestionnaireResponse.QuestionnaireResponseItemComponent>()

            // Stuff answers into the item
            for (answer in testAnswers) {
                var component = QuestionnaireResponse.QuestionnaireResponseItemComponent()
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
            val outputList = qrc.getQuestionnaireAnswers(qr)
            assert(outputList is List<String>)
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
