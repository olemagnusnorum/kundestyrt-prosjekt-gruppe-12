package com.backend.plugins

import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


fun Application.venterBarnRoute() {
    routing {
        route("/venter-barn") {
            get {
                call.respondTemplate("/venter-barn/index.ftl")
            }
        }

    }
}