package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.response.*


fun Application.funksjonsvurderingRoute() {

    routing {

        get("/funksjonsvurdering") {
            call.respondTemplate("funksjonsvurdering/index.ftl")
        }

        //dummy epic server endpoint
        get("/funksjonsvurdering/sometest") {
            val epicResponse = "Aight her gjør vi funksjonsvurderingsting-"
            call.respondText(epicResponse)
        }

    }

}
