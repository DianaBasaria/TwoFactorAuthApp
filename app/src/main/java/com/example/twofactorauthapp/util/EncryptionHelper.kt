package com.example.twofactorauthapp.util

import android.content.Context
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object EncryptionHelper {

    private const val AES_KEY_PREF = "aes_key"
    private const val PREFS_NAME = "app_prefs"

    private lateinit var secretKey: SecretKey

    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_LENGTH_BIT = 128

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val keyBase64 = prefs.getString(AES_KEY_PREF, null)
        secretKey = if (keyBase64 == null) {
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            val key = SecretKeySpec(keyBytes, "AES")
            prefs.edit().putString(AES_KEY_PREF, Base64.encodeToString(keyBytes, Base64.DEFAULT)).apply()
            key
        } else {
            val keyBytes = Base64.decode(keyBase64, Base64.DEFAULT)
            SecretKeySpec(keyBytes, "AES")
        }
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = combined.copyOfRange(0, IV_SIZE)
        val encryptedBytes = combined.copyOfRange(IV_SIZE, combined.size)
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decrypted = cipher.doFinal(encryptedBytes)
        return String(decrypted, Charsets.UTF_8)
    }
}
