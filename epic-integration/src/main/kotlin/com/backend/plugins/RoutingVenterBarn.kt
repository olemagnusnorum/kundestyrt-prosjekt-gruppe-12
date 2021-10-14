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


fun Application.routingVenterBarn() {

    routing {

        //dummy epic server endpoint
        get("/venterBarn/sometest") {
            val epicResponse = "Aight her gjør vi venter barn ting"
            call.respondText(epicResponse)
        }

    }

}
