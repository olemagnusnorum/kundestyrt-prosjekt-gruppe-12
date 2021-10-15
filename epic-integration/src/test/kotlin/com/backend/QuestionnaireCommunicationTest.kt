package com.backend

import com.backend.plugins.QuestionnaireCommunication
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Questionnaire
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

    }

}
