package com.a4grid.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val A4GridLightColorScheme = lightColorScheme()

@Composable
fun A4GridTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = A4GridLightColorScheme,
        content = content
    )
}
