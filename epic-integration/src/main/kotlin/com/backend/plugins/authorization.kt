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
    MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDKx8jcxVpI+OcU
    bc/SuBGA0mVEPHUf/OF1/xkJyR+x/kZOc85hMOk2gsMomYYL/Ie78ZhjhiQLXHPn
    P8fEQnwQyB/vOF2OZ3bbfGHWfvX79Xf/19M6qD1phUGqvkjtjjcwEzblItgKuvTv
    rNN1G/8aGoOtkMD5CePwCRo3bCZt09qTytsEYPzLcXZeY0CcwgUlAYdQrkxTvJjq
    QOoG7RholX3+/YfxUhPkmML0HJddenO2hUSiIJ2yDLsbbFPZjShRNhNNkJ7uBHs7
    eai9PeY2PfBfxU24/dulKOou4wSIlb6x0bUAdtu/xvLRNNEzWQsKYFishmkrZ7Pn
    uicdWjjlAgMBAAECggEAUJVsYS+cXXtseVXaTOcRLZxu2dscwIWCI54omRbwHY/q
    6KpkzYLeauc0HyJDfufwGOQ9pu9by5gGB1P1Uy0ImLKu3iYMJ9c+CKM5TkBOCL7X
    3TgrnRfWr+Rg4DWWTbFChTxdiFl8eWcqh0SrE9L4Yas/wO3RgiZICs/TN0snSmU9
    MB9dYsQjce/tN07IlumJ6b55xFWPubemYr1KMX/E8y1ndIpvdCu0pJsXQih9KqVd
    7GTP84Howr3pHsWmQqhwCR8sm1DvHHXCp84yh0F6Q5jDT2QAKLUDtRZltAy96WM+
    GlZB+lHK10NhCy7+DzaBiW+RjOwNkoyS8m3zb1QFUQKBgQDtGwuJp0b/rR+mrtNv
    vhAyHjWNHoOOPmR8bi3ioGEuyS6zWlC9sGfXPfRg8A931LHXetQchriqP0zdTKx7
    xEgRwIdpA46rSi7ikMhwDqfJ4K7AwVbZmWS+6IE2ljkSCqPkhLTj0MJN9sdNY31R
    zLl/tWByPmMzY5dhUXxi8RB19wKBgQDa8IE4Qf8SzTZqNp6PqfK6+I9QeLXPWeG5
    OOwYR6mc7GcKegkb5EAjkB2QAXVw0DMeC03L5yxhpoFQDyocVhQwdeAIHeq67frM
    yYJe4oaGmbUFT6USIsV1fxoC1miU0Ly9ELirv/cvw+eerhSDEzlZZg2Dnnumtz1g
    UZVFH6whAwKBgQCqSIEZxDwWRWR01hvdpZeGFvIIvoBmuOJ8BrkHiH0jF7NldZiF
    EtsQ11rZ/65sNiHfCkSplAutoTZ3vKgnXwkPTsExJy/gTq+rYaXB7JSORMHchz3z
    mtmizeJ2rJ6iGWgpJzGuZ3AUhAYQy0HmbGxQjpeMlMez0XOXnekCOfTDcwKBgFWQ
    ux/Ogy7I7uRR1mRVSh0SeYg8tOmFujcWbQcMMti2d4rbqOWyrfJ4J2WZfFmLtP6f
    8lanUWwUk+NDGfUbGybZWqYxcwR9b5BLjN0icOr39YxO1TeiSUOjWfoT66mFbs4B
    U/sjuip7yPD2HeQgRkCPJubysGa2pJEZMVpjh3UbAoGBALTy9NUK9S8dpu0bNHjV
    YuoYKbFvtt1sTPSwLqNCa9aNpa/mAdfAUam9EyN/qBoDkH7hG/ivtSJbCn9dIbHJ
    lEaaRsis1Cc1MiNM/KRU2xLz+sp+G2CNJtpMwYQhWv2dYQU19YiHPfEajPRCRcCU
    T84bdDBT4rj0ijvmX0FxHJPq
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

