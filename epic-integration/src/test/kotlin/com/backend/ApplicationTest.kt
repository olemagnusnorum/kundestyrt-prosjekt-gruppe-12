package com.backend

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import kotlin.test.*
import io.ktor.server.testing.*
import com.backend.plugins.*
import com.google.common.base.Predicates.instanceOf
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.function.Executable

class ApplicationTest {

    @Test
    fun testAuthentication() {
        // Asserts that getEpicAccessToken() runs without throwing any exceptions
        assertAll(Executable { runBlocking { getEpicAccessToken() } })
    }

}