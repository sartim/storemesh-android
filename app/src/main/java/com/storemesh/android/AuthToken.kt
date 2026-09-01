package com.storemesh.android

import android.util.Base64
import org.json.JSONObject

/** Reads the OIDC subject locally so BFF customer-scoped requests use the authenticated account. */
fun accessTokenSubject(token: String): String = runCatching {
    val payload = token.split('.').getOrNull(1) ?: return ""
    val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    JSONObject(String(decoded, Charsets.UTF_8)).optString("sub")
}.getOrDefault("")
