package com.ethohampton.yesno
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*

internal fun applyDevelopmentRouting(routing: Routing) {
    val client = HttpClient()
    routing.get ("/{...}") {
        var target = call.request.uri.replaceAfter("?","").replace("?","")
        if (!target.endsWith(".js") &&
            !target.endsWith(".css") &&
            !target.endsWith(".html") &&
            !target.endsWith(".map") &&
            !target.endsWith(".svg") && //for assets
            target != "/"
        ) {
            println("not proxying $target")
            this.proceed()
            return@get
        }

        if(target == "/"){
            target = "/index.html"
        }

        println("proxying request for $target")
        val response = client.request<HttpResponse>("http://localhost:3000${target}")

        val proxiedHeaders = response.headers
        val location = proxiedHeaders[HttpHeaders.Location]
        val contentType = proxiedHeaders[HttpHeaders.ContentType]
        val contentLength = proxiedHeaders[HttpHeaders.ContentLength]

        call.respond(object : OutgoingContent.WriteChannelContent() {
            override val contentLength: Long? = contentLength?.toLong()
            override val contentType: ContentType? = contentType?.let { ContentType.parse(it) }
            override val headers: Headers = Headers.build {
                appendAll(proxiedHeaders.filter { key, _ ->
                    !key.equals(
                        HttpHeaders.ContentType,
                        ignoreCase = true
                    ) && !key.equals(
                        HttpHeaders.ContentLength,
                        ignoreCase = true
                    ) && !key.equals(
                        HttpHeaders.TransferEncoding,
                        ignoreCase = true
                    )
                })
            }
            override val status: HttpStatusCode = response.status
            override suspend fun writeTo(channel: ByteWriteChannel) {
                response.content.copyAndClose(channel)
            }
        })
    }
}