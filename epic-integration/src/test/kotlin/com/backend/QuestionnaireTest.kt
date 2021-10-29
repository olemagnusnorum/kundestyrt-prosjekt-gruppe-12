package com.backend

import com.backend.plugins.QuestionnaireCommunication
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Questionnaire
import org.junit.jupiter.api.*
import kotlin.test.*
import kotlin.test.Test


// Warning: PER_CLASS Lifecycle means that the same QuestionnaireCommunicationTest class is used for every nested test
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionnaireTest {

    private val qc = QuestionnaireCommunication("local")

    @Test
    @Order(1)
    fun `createDefaultQuestionnaires should generate 3 default Questionnaires`() {
        qc.createDefaultQuestionnaires()
        val questionnaireBundle = runBlocking { questionnaireCommunication.searchQuestionnaires() }
        assert(questionnaireBundle.entry.size>3)
    }

    @Test
    @Order(2)
    fun `searchQuestionnaires should return a bundle of Questionnaires`() {
        val questionnaireBundle = runBlocking { questionnaireCommunication.searchQuestionnaires() }
        assert(questionnaireBundle is Bundle)

        // First and last resources should be Questionnaires
        val firstResource = questionnaireBundle.entry.first().resource
        val lastResource = questionnaireBundle.entry.last().resource
        assert(firstResource is Questionnaire)
        assert(lastResource is Questionnaire)
    }

    @Test
    @Order(3)
    fun `createQuestionnaire and getQuestionnaire should generate and get a Questionnaire back`() {
        // Create http parameters for the questionnaire
        val questions = Parameters.build {
            append("testQuestion1", "First test value")
            append("testQuestion2", "Second test value")
            append("testQuestion3", "Third test value")
        }

        // Generate a questionnaire
        val questionnaireID : String = runBlocking { qc.createQuestionnaire(questions, "Sanser") }

        // Get questionnaires back from server and check if they contain correct questions
        val questionnaire : Questionnaire = runBlocking { qc.getQuestionnaire(questionnaireID) }

        assert(questionnaire.item[0].text == questions["testQuestion1"])
        assert(questionnaire.item[1].text == questions["testQuestion2"])
        assert(questionnaire.item[2].text == questions["testQuestion3"])
    }

    @Test
    @Order(4)
    fun `getQuestionnaireQuestions should return the questions within a Questionnaire`() {
        // Create http parameters for the questionnaire
        val questions = Parameters.build {
            append("testQuestion1", "First test value")
            append("testQuestion2", "Second test value")
            append("testQuestion3", "Third test value")
        }

        // Generate a questionnaire
        val questionnaireID : String = runBlocking { qc.createQuestionnaire(questions, "Sanser") }

        // Get questionnaires back from server
        val questionnaire : Questionnaire = runBlocking { qc.getQuestionnaire(questionnaireID) }

        // Extract questions and check if they are what they should be
        val listOfQuestions : List<String> = qc.getQuestionnaireQuestions(questionnaire)

        assert(listOfQuestions[0] == questions["testQuestion1"])
        assert(listOfQuestions[1] == questions["testQuestion2"])
        assert(listOfQuestions[2] == questions["testQuestion3"])
    }
}
