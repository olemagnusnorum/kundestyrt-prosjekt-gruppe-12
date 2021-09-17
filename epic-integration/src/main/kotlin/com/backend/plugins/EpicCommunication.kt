package com.backend.plugins

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun requestEpic() {

    val serverURLString: String = "http://0.0.0.0:8080/epic"
    val url = URL(serverURLString)

    with(url.openConnection() as HttpURLConnection){
        requestMethod = "GET"
        println("THIS IS THE RESPONSE from $serverURLString")
        println("Response code $responseCode")

        inputStream.bufferedReader().use {
            it.lines().forEach{ line ->
                println(line)
            }
        }
    }
}

