package com.example.twofactorauthapp

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TOTPGenerator(secret: String) {

    private val secretKey: ByteArray

    init {
        val base32 = Base32()
        secretKey = base32.decode(secret)
    }

    fun generateCurrentCode(): String {
        val timeWindow = System.currentTimeMillis() / 1000 / 30
        return generateCode(timeWindow)
    }

    private fun generateCode(timeWindow: Long): String {
        val data = ByteBuffer.allocate(8).putLong(timeWindow).array()
        val mac = Mac.getInstance("HmacSHA1")
        val keySpec = SecretKeySpec(secretKey, "HmacSHA1")
        mac.init(keySpec)
        val hash = mac.doFinal(data)
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val truncatedHash = hash.copyOfRange(offset, offset + 4)
        var code = ByteBuffer.wrap(truncatedHash).int and 0x7FFFFFFF
        code %= 1_000_000
        return String.format("%06d", code)
    }

    fun secondsUntilNextCode(): Int {
        val currentTimeSeconds = System.currentTimeMillis() / 1000
        return (30 - (currentTimeSeconds % 30)).toInt()
    }
}
