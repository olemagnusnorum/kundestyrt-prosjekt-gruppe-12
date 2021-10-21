package com.backend

import com.backend.plugins.QuestionnaireResponseCommunication
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import kotlin.test.*


// Warning: PER_CLASS Lifecycle means that the same QuestionnaireCommunicationTest class is used for every nested test
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionnaireResponseCommunicationTest {

    private val qrc = QuestionnaireResponseCommunication("local")

    @Test
    fun `getQuestionnaireAnswers should return a list of strings`() {
        val questionnaireResponseID = "2644277" // Mathias made a questionnaire with this ID
        val questionnaireResponse: QuestionnaireResponse = runBlocking { qrc.getQuestionnaireResponse(questionnaireResponseID) }
        assert(questionnaireResponse is QuestionnaireResponse)

        val returnVal = qrc.getQuestionnaireAnswers(questionnaireResponse)
        assert(returnVal is MutableList<String>)
        assert(returnVal[0] == "fwjnkjefn")
        assert(returnVal[1] == "gknlwfe")
    }


    @Nested
    inner class GeneralTesting {

        @Test
        fun `should return the QuestionnaireResponse items that Mathias generated`() {
            // Mathias made a QuestionnaireResponse with the following stuff
            val itemText1 = "ta da"
            val itemText2 = "tra la la"
            val questionnaireResponseID = "2644277"

            // We should be able to find this on the server with the following test
            val qr: QuestionnaireResponse = runBlocking { qrc.getQuestionnaireResponse(questionnaireResponseID) }
            assert(qr.item[0].text == itemText1)
            assert(qr.item[1].text == itemText2)
        }

    }

}
