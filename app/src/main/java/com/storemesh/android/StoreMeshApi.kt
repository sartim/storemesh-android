package com.storemesh.android

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/** REST client for the local BFF. Domain services remain behind the BFF. */
class StoreMeshApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun login(email: String, password: String): LoginResult {
        val response = request("/api/v1/auth/login", "POST", JSONObject().put("email", email).put("password", password))
        return LoginResult(response.getString("accessToken"), response.getString("refreshToken"))
    }

    fun refresh(refreshToken: String): LoginResult {
        val response = request("/api/v1/auth/refresh", "POST", JSONObject().put("refreshToken", refreshToken))
        return LoginResult(response.getString("accessToken"), response.getString("refreshToken"))
    }

    fun products(accessToken: String): List<Product> {
        val response = request("/api/v1/products?page_size=100&status=PRODUCT_STATUS_ACTIVE", "GET", token = accessToken)
        val array = response.optJSONArray("products") ?: return emptyList()
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            Product(item.optString("id"), item.optString("name"), item.optString("description"), item.optString("priceMinor", "0").toLongOrNull() ?: 0L, item.optString("currency", "USD"))
        }
    }

    fun orders(accessToken: String): List<Order> {
        val response = request("/api/v1/orders?page_size=50", "GET", token = accessToken)
        val array = response.optJSONArray("orders") ?: return emptyList()
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            Order(item.optString("orderId"), item.optString("status", "ORDER_STATUS_PENDING"), item.optLong("totalMinor"), item.optString("currency", "USD"), item.optString("createdAt"))
        }
    }

    fun createOrder(accessToken: String, customerId: String, lines: List<CartLine>): Order {
        require(lines.isNotEmpty()) { "cart cannot be empty" }
        val payload = JSONObject().put("customerId", customerId).put("lines", org.json.JSONArray().apply {
            lines.forEach { put(JSONObject().put("productId", it.productId).put("quantity", it.quantity)) }
        })
        val response = request(
            "/api/v1/orders",
            "POST",
            payload,
            accessToken,
            idempotencyKey = java.util.UUID.randomUUID().toString()
        )
        val item = response.getJSONObject("order")
        return Order(item.optString("orderId"), item.optString("status"), item.optLong("totalMinor"), item.optString("currency", "USD"), item.optString("createdAt"))
    }

    private fun request(path: String, method: String, body: JSONObject? = null, token: String? = null, idempotencyKey: String? = null): JSONObject {
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/json")
            if (token != null) setRequestProperty("Authorization", "Bearer $token")
            if (idempotencyKey != null) setRequestProperty("Idempotency-Key", idempotencyKey)
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                outputStream.use { it.write(body.toString().toByteArray()) }
            }
        }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val payload = BufferedReader(InputStreamReader(stream)).use { it.readText() }
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(JSONObject(payload).optString("message", "Request failed (${connection.responseCode})"))
        }
        return JSONObject(payload)
    }
}
