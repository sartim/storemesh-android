package com.storemesh.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class Product(val id: String, val name: String, val description: String, val priceMinor: Long, val currency: String) { fun formattedPrice() = "${currency.ifBlank { "USD" }} ${"%.2f".format(priceMinor / 100.0)}" }
data class LoginResult(val accessToken: String, val refreshToken: String)
data class Order(val orderId: String, val status: String, val totalMinor: Long, val currency: String, val createdAt: String)
data class CartLine(val productId: String, val quantity: Int)
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("storemesh_session", Context.MODE_PRIVATE)

    val accessToken: String?
        get() = read("access_token")
    val refreshToken: String?
        get() = read("refresh_token")

    fun save(session: LoginResult) {
        write("access_token", session.accessToken)
        write("refresh_token", session.refreshToken)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun key(): SecretKey {
        val store = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey()
    }

    private fun write(name: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key())
        }
        val encrypted = cipher.iv + cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        prefs.edit().putString(name, Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
    }

    private fun read(name: String): String? = runCatching {
        val encoded = prefs.getString(name, null) ?: return null
        val encrypted = Base64.decode(encoded, Base64.NO_WRAP)
        if (encrypted.size <= IV_SIZE) return null
        Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(GCM_TAG_BITS, encrypted.copyOfRange(0, IV_SIZE))
            )
        }.doFinal(encrypted.copyOfRange(IV_SIZE, encrypted.size))
            .toString(StandardCharsets.UTF_8)
    }.getOrNull()

    private companion object {
        const val KEY_ALIAS = "storemesh_session_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val GCM_TAG_BITS = 128
    }
}
