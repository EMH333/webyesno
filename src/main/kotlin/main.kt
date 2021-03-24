import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.util.pipeline.*
import java.net.Inet6Address
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

var cookieValue: String = ""
var pastCookieValue: String = ""
var devMode: Boolean = false
fun main() {
    timer(name = "updateCookie", period = TimeUnit.HOURS.toMillis(1)) {
        pastCookieValue = cookieValue
        cookieValue = getRandomString(10)
    }
    val server = embeddedServer(
        Netty,
        port = 8000,
        module = Application::myRouting
    )
    server.start(wait = false)//note that the server will keep program running forever even if wait is false
}

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun Application.myRouting() {
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

        //intercept(HttpRequestPipeline.State) {
        //    call.response.cookies.append("ticket", cookieValue) // always update cookie header
        //}

        static("/") {
            defaultResource("dist/index.html")
            if (!devMode) {
                resources("dist")
            }
        }

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

            when (call.receiveText().toLowerCase()) {
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
            call.response.cookies.append(name = "submitted", value = true.toString(), expires = GMTDate().plus(Duration.ofDays(1).toMillis()))
            call.respondText("OK")
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
