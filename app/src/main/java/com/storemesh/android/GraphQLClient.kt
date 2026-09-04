package com.storemesh.android

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

/** Small transport for the BFF GraphQL endpoint; domain services remain private. */
class GraphQLClient(private val endpoint: String = BuildConfig.API_BASE_URL.trimEnd('/') + "/api/v1/graphql") {
    fun execute(query: String, accessToken: String): JSONObject {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8_000
            readTimeout = 8_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $accessToken")
            outputStream.use { it.write(JSONObject().put("query", query).toString().toByteArray()) }
        }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val payload = BufferedReader(InputStreamReader(stream)).use { it.readText() }
        val response = JSONObject(payload)
        if (connection.responseCode !in 200..299 || response.optJSONArray("errors")?.length() ?: 0 > 0) {
            throw IllegalStateException(response.optJSONArray("errors")?.optJSONObject(0)?.optString("message") ?: "GraphQL request failed (${connection.responseCode})")
        }
        return response.optJSONObject("data") ?: throw IllegalStateException("GraphQL response did not contain data")
    }

    fun products(accessToken: String): List<Product> {
        val data = execute("""{ products(pageSize: 100) { products { id name description priceMinor currency } } }""", accessToken)
        val items = data.optJSONObject("products")?.optJSONArray("products") ?: JSONArray()
        return List(items.length()) { index ->
            val item = items.getJSONObject(index)
            Product(item.optString("id"), item.optString("name"), item.optString("description"), item.optLong("priceMinor"), item.optString("currency", "USD"))
        }
    }

    fun cart(accessToken: String): List<CartLine> {
        val lines = execute("""{ cart { lines { productId quantity } } }""", accessToken).optJSONObject("cart")?.optJSONArray("lines") ?: JSONArray()
        return List(lines.length()) { index ->
            val item = lines.getJSONObject(index)
            CartLine(item.optString("productId"), item.optInt("quantity"))
        }
    }
}
