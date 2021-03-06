package com.ethohampton.yesno

import com.ethohampton.yesno.login.applyLoginToApplication
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.util.pipeline.*
import java.net.Inet6Address
import java.time.Duration
import java.util.*

var devMode: Boolean = false
fun main() {
    val server = embeddedServer(
        CIO,
        port = 8000,
        module = Application::myRouting,
    )
    server.start(wait = true)
}

fun Application.myRouting() {
    applyLoginToApplication(this)

    routing {
        install(CachingHeaders) {
            options { outgoingContent ->
                when {
                    outgoingContent.headers.contains("Cache-Control") ||
                            outgoingContent.headers.contains("cache-control") -> {
                        null
                    }
                    else -> {
                        CachingOptions(
                            cacheControl = CacheControl.MaxAge(
                                maxAgeSeconds = 60,
                                proxyMaxAgeSeconds = null,
                                mustRevalidate = false,
                                proxyRevalidate = false,
                                visibility = CacheControl.Visibility.Private
                            )
                        )
                    }
                }
            }
        }

        if ((System.getenv("DEV") ?: "false") == "true") {
            applyDevelopmentRouting(this)
            println("In development mode")
            devMode = true
        }

        //trace { application.log.info(it.buildText()) }

        get("/result") {
            if (devMode) {
                call.response.cacheControl(CacheControl.NoCache(null))//don't cache these responses when in dev mode
            }
            call.respondText(Counter.getResult())
        }

        post("/answer") {
            call.response.cacheControl(CacheControl.NoCache(null))//don't cache this

            //already submitted
            if (call.request.cookies["submitted"] == true.toString() && !devMode) {
                call.respondText("OK")
                return@post
            }

            when (call.receiveText().lowercase(Locale.getDefault())) {
                "yes" -> {
                    Counter.addYes(call.getLikelyIP())
                }
                "no" -> {
                    Counter.addNo(call.getLikelyIP())
                }
                else -> {
                    call.respondText("ERROR")
                    return@post
                }
            }
            call.response.cookies.append(
                name = "submitted",
                value = true.toString(),
                expires = GMTDate().plus(Duration.ofDays(1).toMillis()),
                extensions = mapOf(Pair("SameSite", "Strict"))
            )
            call.respondText("OK")
        }

        static("/") {
            //install(Compression)
            defaultResource("dist/index.html")
            if (!devMode) {
                //seems it can't serve precompressed resources
                //preCompressed(CompressedFileType.BROTLI, CompressedFileType.GZIP) {
                //    resources("dist/precompressed")
                //}
                resources("dist")
            }
        }
    }
}

fun ApplicationCall.getLikelyIP(): ByteArray {
    val host = request.origin.remoteHost
    // Try parsing this as an IPV6 address.
    runCatching {
        Inet6Address.getByName("[$host]")
    }.onSuccess {
        // This is an IPv6 address: only take the first 64 bits (8 bytes)
        // as the caller key
        return it.address.copyOf(8)
    }
    return host.toByteArray()
}
