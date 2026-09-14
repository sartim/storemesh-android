package com.storemesh.android

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

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
}
