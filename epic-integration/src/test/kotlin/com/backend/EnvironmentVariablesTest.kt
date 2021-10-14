package com.backend

import org.junit.jupiter.api.Test

class EnvironmentVariablesTest {

    @Test
    fun `Environment variables should be accessible in the test environment`() {
        /**
         * If this test fails then the environment variables are not accessible in the test environment (or not at all).
         * To add the environment variables to all future test evironments, go to:
         *  - Run | Edit Configurations | Edit configuration templates | Gradle
         *
         * From there, add the environment variables.
         * Remove all Gradle run configurations that were made before editing the template.
         * Rerun the test. It should now have access to the variables.
         */
        val privateKey = System.getenv("epic_private_key")
        assert(privateKey is String)
        assert(privateKey.isNotEmpty())

        val clientId = System.getenv("epic_client_id")
        assert(clientId is String)
        assert(clientId.isNotEmpty())
    }

}