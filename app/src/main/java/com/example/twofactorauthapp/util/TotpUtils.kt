package com.example.twofactorauthapp.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base32

object TotpUtils {

    private const val TIME_STEP_SECONDS = 30L
    private const val TOTP_LENGTH = 6

    private val base32 = Base32()

    /**
     * აგენერირებს TOTP კოდს მოცემული საიდუმლო გასაღებით.
     * @param secretBase32 base32-ით დაკოდირებული საიდუმლო გასაღები
     * @param timeSeconds UNIX დრო წამებში (ნაგულისხმევად: მიმდინარე დრო UTC-ში)
     */
    fun generateTOTP(
        secretBase32: String,
        timeSeconds: Long = System.currentTimeMillis() / 1000
    ): String {
        val key = base32.decode(secretBase32.replace(" ", "").uppercase())

        val timeWindow = timeSeconds / TIME_STEP_SECONDS

        val data = ByteBuffer.allocate(8)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(timeWindow)
            .array()

        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(data)

        val offset = hash[hash.size - 1].toInt() and 0x0f
        val truncatedHash = hash.copyOfRange(offset, offset + 4)
        val binary = ByteBuffer.wrap(truncatedHash).int and 0x7FFFFFFF
        val otp = binary % 1_000_000

        return otp.toString().padStart(TOTP_LENGTH, '0')
    }
}
