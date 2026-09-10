package com.storemesh.android

import java.util.Base64

/** Reads the OIDC subject locally so BFF customer-scoped requests use the authenticated account. */
fun accessTokenSubject(token: String): String = runCatching {
    val payload = token.split('.').getOrNull(1) ?: return ""
    val decoded = Base64.getUrlDecoder().decode(payload)
    Regex("\\\"sub\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(String(decoded, Charsets.UTF_8))?.groupValues?.get(1).orEmpty()
}.getOrDefault("")
