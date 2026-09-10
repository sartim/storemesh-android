package com.storemesh.android

import org.junit.Assert.assertEquals
import org.junit.Test

class CommerceModelsTest {
    @Test
    fun productFormatsMinorUnitsWithCurrency() {
        val product = Product("p-1", "SM-LAMP-001", "Desk lamp", "", 1299, "USD")

        assertEquals("USD 12.99", product.formattedPrice())
    }

    @Test
    fun cartLineRetainsProductIdentityAndQuantity() {
        val line = CartLine("p-1", 2)

        assertEquals("p-1", line.productId)
        assertEquals(2, line.quantity)
    }

    @Test
    fun accessTokenSubjectReadsOidcSubject() {
        val payload = "eyJzdWIiOiJjdXN0b21lci0xIn0="

        assertEquals("customer-1", accessTokenSubject("header.$payload.signature"))
    }

    @Test
    fun malformedAccessTokenHasNoSubject() {
        assertEquals("", accessTokenSubject("not-a-jwt"))
    }
}
