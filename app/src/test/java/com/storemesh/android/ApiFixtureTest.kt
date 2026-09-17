package com.storemesh.android

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.Base64

class ApiFixtureTest {
    private lateinit var server: HttpServer
    private lateinit var baseUrl: String

    @Before
    fun startFixture() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange -> respond(exchange) }
        server.start()
        baseUrl = "http://127.0.0.1:${server.address.port}"
    }

    @After
    fun stopFixture() = server.stop(0)

    @Test
    fun bffCatalogCartAndOrderContractsDecode() = runBlocking {
        val token = customerToken("customer-1")
        val graphQLProducts = GraphQLClient("$baseUrl/api/v1/graphql").products(token)
        assertEquals("Halo desk lamp", graphQLProducts.single().name)

        val api = StoreMeshApi(baseUrl)
        val products = api.products(token)
        assertEquals("p-1", products.single().id)
        assertEquals(listOf(CartLine("p-1", 1)), api.getCart(token))

        val saved = api.saveCart(token, listOf(CartLine("p-1", 2)))
        assertEquals(listOf(CartLine("p-1", 2)), saved)
        assertEquals("o-1", api.createOrder(token, "customer-1", saved).orderId)
    }

    private fun respond(exchange: HttpExchange) {
        val requestBody = exchange.requestBody.bufferedReader().use { it.readText() }
        val path = exchange.requestURI.path
        val body = when {
            path.endsWith("/graphql") && requestBody.contains("products") -> "{\"data\":{\"products\":{\"products\":[{\"id\":\"p-1\",\"sku\":\"SM-LAMP-001\",\"name\":\"Halo desk lamp\",\"description\":\"Warm light\",\"priceMinor\":1299,\"currency\":\"USD\"}]}}}"
            path.endsWith("/products") -> "{\"products\":[{\"id\":\"p-1\",\"sku\":\"SM-LAMP-001\",\"name\":\"Halo desk lamp\",\"description\":\"Warm light\",\"priceMinor\":\"1299\",\"currency\":\"USD\"}]}"
            path.endsWith("/cart") && exchange.requestMethod == "PUT" -> "{\"lines\":[{\"productId\":\"p-1\",\"quantity\":2}]}"
            path.endsWith("/cart") -> "{\"lines\":[{\"productId\":\"p-1\",\"quantity\":1}]}"
            path.endsWith("/orders") -> "{\"order\":{\"orderId\":\"o-1\",\"status\":\"ORDER_STATUS_PENDING\",\"totalMinor\":2598,\"currency\":\"USD\",\"createdAt\":\"2026-09-17T00:00:00Z\"}}"
            else -> "{}"
        }
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun customerToken(subject: String): String {
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"sub\":\"$subject\"}".toByteArray())
        return "header.$payload.signature"
    }
}
