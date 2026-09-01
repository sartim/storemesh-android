package com.storemesh.android

import android.content.Context

data class Product(val id: String, val name: String, val description: String, val priceMinor: Long, val currency: String) { fun formattedPrice() = "${currency.ifBlank { "USD" }} ${"%.2f".format(priceMinor / 100.0)}" }
data class LoginResult(val accessToken: String, val refreshToken: String)
data class Order(val orderId: String, val status: String, val totalMinor: Long, val currency: String, val createdAt: String)
class SessionStore(context: Context) { private val prefs = context.getSharedPreferences("storemesh_session", Context.MODE_PRIVATE); val accessToken get() = prefs.getString("access_token", null); val refreshToken get() = prefs.getString("refresh_token", null); fun save(session: LoginResult) { prefs.edit().putString("access_token", session.accessToken).putString("refresh_token", session.refreshToken).apply() }; fun clear() { prefs.edit().clear().apply() } }
