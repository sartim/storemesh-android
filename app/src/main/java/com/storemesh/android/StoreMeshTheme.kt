package com.storemesh.android

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StoreMeshLightColors = lightColorScheme(
    primary = Color(0xFF155EEF),
    onPrimary = Color.White,
    secondary = Color(0xFF10B981),
    tertiary = Color(0xFFF59E0B),
    background = Color(0xFFFAFAF8),
    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun StoreMeshTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = StoreMeshLightColors, content = content)
}
