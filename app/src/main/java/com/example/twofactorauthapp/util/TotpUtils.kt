package com.example.twofactorauthapp.util

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TotpUtils {

    private const val DEFAULT_TIME_STEP_SECONDS = 30L
    private const val TOTP_LENGTH = 6

    /**
     * გენერირდება TOTP კოდის მიცემული საიდუმლო გასაღებით
     */
    fun generateTOTP(secret: String, time: Long = System.currentTimeMillis() / 1000): String {
        val timeStep = time / DEFAULT_TIME_STEP_SECONDS

        val key = Base64.decode(secret, Base64.DEFAULT)

        val data = ByteBuffer.allocate(8)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(timeStep)
            .array()

        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))

        val hash = mac.doFinal(data)

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val truncatedHash = hash.copyOfRange(offset, offset + 4)

        val binary = ByteBuffer.wrap(truncatedHash).int and 0x7FFFFFFF
        val otp = binary % 1_000_000

        return otp.toString().padStart(TOTP_LENGTH, '0')
    }
}
