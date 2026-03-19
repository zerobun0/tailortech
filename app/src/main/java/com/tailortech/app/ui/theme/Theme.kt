package com.tailortech.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TailorDarkColors = darkColorScheme(
    primary = AccentLime,
    onPrimary = BlackBg,
    secondary = AccentOrange,
    background = BlackBg,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    outline = Border
)

@Composable
fun TailorTechTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TailorDarkColors,
        typography = TailorTypography,
        content = content
    )
}
