package com.backend

import com.backend.plugins.EpicCommunication
import org.junit.Test

class EpicCommunicationTest {

    private val epicCommunication = EpicCommunication()

    @Test
    suspend fun `Patient_Search returns a string`() {
        assert(epicCommunication.patientSearch(given = "Derrick", family = "Lin", birthdate = "1973-06-03") is String)
    }

}