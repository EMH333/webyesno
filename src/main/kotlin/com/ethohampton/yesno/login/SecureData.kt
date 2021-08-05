package com.ethohampton.yesno.login

import io.ktor.util.*
import java.security.SecureRandom
import java.util.*

fun ByteArray.toBase64(): String =
    String(Base64.getEncoder().encode(this))

object SecureData {
    private val secureRandom = SecureRandom()

    fun getNewSessionIdentifier(bits: Int = 64): String {
        //round up # of bytes
        val raw = ByteArray(((bits/8.0) + 0.5).toInt())
        secureRandom.nextBytes(raw) //fill them

        return raw.toBase64()
    }
}