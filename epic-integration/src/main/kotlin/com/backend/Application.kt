package com.backend

import com.backend.plugins.*

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.backend.plugins.*
import com.sun.net.httpserver.Authenticator
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.security.Key

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        install(Authentication) {
            jwt {
                // Configure jwt authentication
            }
        }

        //launch { println(getEpicAccessToken()) }



        personRoute()
    }.start(wait = true)
}
