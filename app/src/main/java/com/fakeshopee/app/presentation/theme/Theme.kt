package com.fakeshopee.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FakeShopeeOrange = Color(0xFFEE4D2D)
val FakeShopeeNavy = Color(0xFF07006C)
val FakeShopeeMint = Color(0xFF6CF8BB)
val FakeShopeeMintDark = Color(0xFF006C49)
val FakeShopeeBackground = Color(0xFFFAF8FF)
val FakeShopeeSurface = Color(0xFFEAEDFF)
val FakeShopeeOnSurface = Color(0xFF131B2E)
val FakeShopeeSubtext = Color(0xFF464554)
val FakeShopeeBorder = Color(0xFFDAE2FD)
val FakeShopeeError = Color(0xFFBA1A1A)

private val LightColorScheme = lightColorScheme(
    primary = FakeShopeeOrange,
    onPrimary = Color.White,
    primaryContainer = FakeShopeeSurface,
    onPrimaryContainer = FakeShopeeNavy,
    secondary = FakeShopeeMint,
    onSecondary = Color(0xFF002113),
    background = FakeShopeeBackground,
    surface = Color.White,
    onBackground = FakeShopeeOnSurface,
    onSurface = FakeShopeeOnSurface,
    error = FakeShopeeError
)

@Composable
fun FakeShopeeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
