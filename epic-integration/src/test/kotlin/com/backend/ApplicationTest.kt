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

class ApplicationTest {

    @Test
    fun testAuthentication() = withTestApplication(Application::main) {
        assertThat(runBlocking { getEpicAccessToken() }, instanceOf(String::class.java))
    }

}