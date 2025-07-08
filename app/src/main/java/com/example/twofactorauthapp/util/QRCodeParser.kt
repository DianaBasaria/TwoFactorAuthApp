package com.example.twofactorauthapp.util

import android.net.Uri

data class ParsedOtpAuth(
    val label: String,
    val secret: String,
    val issuer: String?
)

object QRCodeParser {

    fun parseOtpAuthUri(uri: String): ParsedOtpAuth? {
        return try {
            val parsedUri = Uri.parse(uri)

            if (parsedUri.scheme != "otpauth") return null
            if (parsedUri.host != "totp") return null

            val label = parsedUri.pathSegments.joinToString("/").removePrefix("/")
            val secret = parsedUri.getQueryParameter("secret") ?: return null
            val issuer = parsedUri.getQueryParameter("issuer")

            ParsedOtpAuth(
                label = label,
                secret = secret,
                issuer = issuer
            )
        } catch (e: Exception) {
            null
        }
    }
}
