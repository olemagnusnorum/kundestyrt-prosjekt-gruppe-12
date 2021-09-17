package com.backend.plugins

import java.util.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotlin.collections.HashMap
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json



@Serializable
data class jsonAccessToken(val access_token :String, val token_type :String, val expires_in :Int, val scope :String)

fun generateJWT(): String {
    //private key er gjort om, men ikke public må være på dette formatet
    //RSA private key is reformated to fit the java library used to make private key class
    // openssl pkcs8 -topk8 -in private_key.pem -inform pem -out private_key_pkcs8.pem -outform pem -nocrypt
    val myPrivateKey = """
    dummy-private-key
    """

    // removing white space
    val regexPrivateKey :String = myPrivateKey.replace("\\s".toRegex(), "")

    // factoring private key to fit PrivateKey java class
    val kf :KeyFactory = KeyFactory.getInstance("RSA")
    val keySpecPKCS8 :PKCS8EncodedKeySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(regexPrivateKey))
    val newPrivateKey :PrivateKey = kf.generatePrivate(keySpecPKCS8)

    //making header for JWT
    val headers : HashMap<String, Any?> = HashMap<String, Any?>()
    headers["alg"] = SignatureAlgorithm.RS384
    headers["typ"] = "JWT"
    // making body for JWT
    val claims : HashMap<String, Any?> = HashMap<String, Any?>()
    claims["iss"] = "f89a9ce8-81e3-4455-b6a8-d22bf38324a7"
    claims["sub"] = "f89a9ce8-81e3-4455-b6a8-d22bf38324a7"
    claims["aud"] = "https://fhir.epic.com/interconnect-fhir-oauth/oauth2/token"
    claims["jti"] = (System.currentTimeMillis() + 300000).toString()
    claims["exp"] = Date(System.currentTimeMillis() + 300000)

    // generating JWT with jjwt library
    // returns a string
    val myJWTToken: String = Jwts.builder()
        .setHeader(headers)
        .setClaims(claims)
        .signWith(newPrivateKey, SignatureAlgorithm.RS384)
        .compact()


    return myJWTToken
}

suspend fun getEpicAccessToken(): String {
    // posting the JWT and getting an access token in response
    // to call the function it has to be inside launch{val token = getEpicAccessToken()}
    println("Response")
    val epicJWT : String = generateJWT()
    val client = HttpClient()

    val response :HttpResponse = client.post("https://fhir.epic.com/interconnect-fhir-oauth/oauth2/token"){
        contentType(ContentType.Application.FormUrlEncoded)
        body = "grant_type=client_credentials&client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer&client_assertion=$epicJWT"
    }

    val jsonString = response.receive<String>()
    val jsonObj = Json.decodeFromString<jsonAccessToken>(jsonString)

    return jsonObj.access_token
}

