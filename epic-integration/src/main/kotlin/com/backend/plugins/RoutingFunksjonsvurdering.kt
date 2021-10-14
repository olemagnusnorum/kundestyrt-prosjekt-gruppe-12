package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.freemarker.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable


fun Application.routingFunksjonsvurdering() {

    routing {

        //dummy epic server endpoint
        get("/funksjonsvurdering/sometest") {
            val epicResponse = "Aight her gjør vi funksjonsvurderingsting-"
            call.respondText(epicResponse)
        }

    }

}
