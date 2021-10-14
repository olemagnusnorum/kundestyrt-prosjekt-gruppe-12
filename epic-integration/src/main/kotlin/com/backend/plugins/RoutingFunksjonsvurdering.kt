package com.backend.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.response.*


fun Application.routingFunksjonsvurdering() {

    routing {

        // Landing page - navigation
        get("/funksjonsvurdering") {
            call.respondTemplate("funksjonsvurdering/index.ftl")
        }

        // Nav landing page
        get("/funksjonsvurdering/nav") {
            call.respondTemplate("funksjonsvurdering/nav.ftl")
        }

        // Doctor landing page
        get("/funksjonsvurdering/doctor") {
            call.respondTemplate("funksjonsvurdering/doctor.ftl")
        }

    }

}
