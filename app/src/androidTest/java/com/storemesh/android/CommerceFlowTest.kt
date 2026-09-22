package com.storemesh.android

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.Assume.assumeTrue
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking

/** Deterministic UI checks for the unauthenticated entry point and saved cart. */
class CommerceFlowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun loginRequiresBothCredentials() {
        compose.setContent { StoreMeshTheme { LoginScreen(onLoggedIn = {}) } }

        compose.onNodeWithText("Welcome back").assertIsDisplayed()
        compose.onNodeWithText("Log in").assertIsNotEnabled()
    }

    @Test
    fun savedCartShowsQuantityAndSubtotal() {
        val product = Product("p-1", "SM-LAMP-001", "Halo desk lamp", "Warm light", 1299, "USD")
        compose.setContent {
            StoreMeshTheme {
                CartScreen(
                    lines = listOf(CartLine(product.id, 2)),
                    products = listOf(product),
                    onChange = { _, _ -> },
                    onClear = {},
                    onCheckout = {}
                )
            }
        }

        compose.onNodeWithText("Saved cart (2)").assertIsDisplayed()
        compose.onNodeWithText("Halo desk lamp").assertIsDisplayed()
        compose.onNodeWithText("Subtotal").assertIsDisplayed()
        compose.onNodeWithText("USD 25.98").assertIsDisplayed()
    }

    @Test
    fun liveBffCommerceFlowWhenConfigured() = runBlocking {
        assumeTrue(BuildConfig.INTEGRATION_BFF_URL.isNotBlank())
        assumeTrue(BuildConfig.INTEGRATION_ACCESS_TOKEN.isNotBlank())
        val api = StoreMeshApi(BuildConfig.INTEGRATION_BFF_URL)
        val token = BuildConfig.INTEGRATION_ACCESS_TOKEN
        val product = api.products(token).firstOrNull() ?: error("BFF returned no active products")
        val saved = api.saveCart(token, listOf(CartLine(product.id, 1)))
        assertTrue(saved.single().productId == product.id)
        val order = api.createOrder(token, accessTokenSubject(token), saved)
        assertTrue(order.orderId.isNotBlank())
        api.clearCart(token)
        assertTrue(api.getCart(token).isEmpty())
    }
}
