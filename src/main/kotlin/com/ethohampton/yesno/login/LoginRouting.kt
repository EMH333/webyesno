package com.ethohampton.yesno.login

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import java.time.ZonedDateTime

@Serializable
data class UserSession(
    val id: String,
    val sessionIdentifier: String = SecureData.getNewSessionIdentifier(),
    @Transient
    //TODO serialize this so it gets included and can invalidate sessions
    val sessionExpiration: ZonedDateTime = ZonedDateTime.now().plusHours(24)
) :
    Principal

@ExperimentalSerializationApi
class UserSessionSerializer : SessionSerializer<UserSession> {
    override fun deserialize(text: String): UserSession {
        return Cbor.decodeFromHexString(text)
    }

    override fun serialize(session: UserSession): String {
        return Cbor.encodeToHexString(session)
    }

}

@ExperimentalSerializationApi
internal fun applyLoginToApplication(application: Application) {
    application.install(Authentication) {
        form("login") {
            userParamName = "email"
            passwordParamName = "password"
            validate { credentials ->
                // make sure we don't authenticate twice if the previous session is still valid
                this.principal<UserSession>()?.let {
                    return@validate null
                }

                //TODO: this is where we authenticate the user, set some sort of session ID
                if (credentials.name == "jetbrains@test.com" && credentials.password == "foobar") {
                    val session = UserSession(credentials.name)
                    sessions.set(session)
                    session
                } else {
                    null
                }
            }
        }

        session<UserSession>("auth-session") {
            validate { session ->
                //TODO: Where we confirm that we have a valid session, should use session ID validation
                if (session.id.startsWith("jetbrains")) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respond(UnauthorizedResponse())
            }
        }
    }

    application.install(Sessions) {
        cookie<UserSession>("user_session", storage = SessionStorageMemory()) {
            serializer = UserSessionSerializer()
        }
    }

    application.routing {

        authenticate("login", "auth-session") {
            post("/login") {
                //set an ID cookie if login successful
                call.principal<UserSession>()?.let {
                    call.response.cookies.append("ID", it.id)
                }
                call.respondText("OK ${call.principal<UserSession>()?.id}!")
            }
            get("/test") {
                println("all the stuff v2")
                call.respondText("Hello, ${call.principal<UserSession>()?.id}!")
            }
        }

        post("/logout") {
            call.sessions.clear<UserSession>()
            call.respondText("OK")
        }
    }
}