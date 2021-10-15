package com.backend

import com.backend.plugins.QuestionnaireCommunication
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import kotlin.test.*


// Warning: PER_CLASS Lifecycle means that the same QuestionnaireCommunicationTest class is used for every nested test
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionnaireCommunicationTest {

    private val qc = QuestionnaireCommunication()

    @Test
    fun `getQuestionnaireQuestions should return a list of strings`() {
        val questionnaireID = "2641197" // Mathias made a questionnaire with this ID
        val questionnaire: Questionnaire = runBlocking { qc.getQuestionnaire(questionnaireID) }
        assert(questionnaire is Questionnaire)

        val returnVal = qc.getQuestionnaireQuestions(questionnaire)
        assert(returnVal is MutableList<String>)
    }

    @Test
    fun `getQuestionnaireAnswers should return a list of strings`() {
        val questionnaireResponseID = "2644277" // Mathias made a questionnaire with this ID
        val questionnaireResponse: QuestionnaireResponse = runBlocking { qc.getQuestionnaireResponse(questionnaireResponseID) }
        assert(questionnaireResponse is QuestionnaireResponse)

        val returnVal = qc.getQuestionnaireAnswers(questionnaireResponse)
        println(returnVal)
        assert(returnVal is MutableList<String>)
        assert(returnVal[0] == "fwjnkjefn")
        assert(returnVal[1] == "gknlwfe")
    }


    @Nested
    inner class GeneralTesting {

        @Test
        fun `should return the Questionnaire resource that Mathias generated`() {
            // Mathias made a Questionnaire with the following stuff
            val itemText1 = "ta da"
            val itemText2 = "tra la la"
            val name = "NavQuestionnaire"
            val title = "Nav questionaire: Sykemelding"
            val publisher = "NAV"
            val questionnaireID = "2641197"

            // We should be able to find this on the server with the following test
            val questionnaire: Questionnaire = runBlocking { qc.getQuestionnaire(questionnaireID) }
            assert(questionnaire.item[0].text == itemText1)
            assert(questionnaire.item[1].text == itemText2)
            assert(questionnaire.name == name)
            assert(questionnaire.title == title)
            assert(questionnaire.publisher == publisher)
        }

        @Test
        fun `should return the QuestionnaireResponse items that Mathias generated`() {
            // Mathias made a QuestionnaireResponse with the following stuff
            val itemText1 = "ta da"
            val itemText2 = "tra la la"
            val questionnaireResponseID = "2644277"

            // We should be able to find this on the server with the following test
            val qr: QuestionnaireResponse = runBlocking { qc.getQuestionnaireResponse(questionnaireResponseID) }
            assert(qr.item[0].text == itemText1)
            assert(qr.item[1].text == itemText2)
        }

    }

}