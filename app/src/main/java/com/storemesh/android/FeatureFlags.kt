package com.storemesh.android

import org.json.JSONObject

data class FeatureFlags(private val values: Map<String, Boolean> = defaults) {
    fun enabled(key: String): Boolean = values[key] ?: defaults[key] ?: false

    companion object {
        val defaults = mapOf(
            "graphql_checkout" to true,
            "admin_dashboard_v2" to true,
            "mobile_cart_v2" to true,
        )

        fun fromJson(json: JSONObject): FeatureFlags {
            val flags = json.optJSONObject("flags") ?: return FeatureFlags()
            return FeatureFlags(defaults.mapValues { (key, fallback) -> flags.optBoolean(key, fallback) })
        }
    }
}
