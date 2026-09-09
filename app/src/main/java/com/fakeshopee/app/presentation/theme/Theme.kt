package com.fakeshopee.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Apex Neo-Commerce & Pay Primary Palette
val FakeShopeeOrange = Color(0xFFEE4D2D)
val FakeShopeePrimary = Color(0xFFB22204)
val FakeShopeePrimaryContainer = Color(0xFFD63C1E)

// Secondary & Navy Palette
val FakeShopeeNavy = Color(0xFF0B132B)
val FakeShopeeSecondary = Color(0xFF565D79)
val FakeShopeeSecondaryContainer = Color(0xFFD8DEFF)
val FakeShopeeSecondaryFixed = Color(0xFFDBE1FF)
val FakeShopeeOnSecondaryFixed = Color(0xFF131A33)

// Tertiary Mint Palette
val FakeShopeeMint = Color(0xFF6CF8BB)
val FakeShopeeMintDark = Color(0xFF006C49)
val FakeShopeeTertiary = Color(0xFF006947)
val FakeShopeeTertiaryContainer = Color(0xFF00855B)
val FakeShopeeTertiaryFixed = Color(0xFF6FFBBE)
val FakeShopeeTertiaryFixedDim = Color(0xFF4EDEA3)
val FakeShopeeOnTertiaryFixed = Color(0xFF002113)

// Surface & Neutral Canvas
val FakeShopeeBackground = Color(0xFFF7F9FB)
val FakeShopeeSurface = Color(0xFFF7F9FB)
val FakeShopeeSurfaceContainerLowest = Color(0xFFFFFFFF)
val FakeShopeeSurfaceContainerLow = Color(0xFFF2F4F6)
val FakeShopeeSurfaceContainer = Color(0xFFECEEF0)
val FakeShopeeSurfaceContainerHigh = Color(0xFFE6E8EA)
val FakeShopeeSurfaceContainerHighest = Color(0xFFE0E3E5)

// Typography & Lines
val FakeShopeeOnSurface = Color(0xFF191C1E)
val FakeShopeeSubtext = Color(0xFF565D79)
val FakeShopeeBorder = Color(0xFFE2E8F0)
val FakeShopeeOutlineVariant = Color(0xFFE3BEB6)

// Error States
val FakeShopeeError = Color(0xFFBA1A1A)
val FakeShopeeErrorContainer = Color(0xFFFFDAD6)

private val LightColorScheme = lightColorScheme(
    primary = FakeShopeePrimary,
    onPrimary = Color.White,
    primaryContainer = FakeShopeePrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = FakeShopeeSecondary,
    onSecondary = Color.White,
    secondaryContainer = FakeShopeeSecondaryContainer,
    tertiary = FakeShopeeTertiary,
    onTertiary = Color.White,
    tertiaryContainer = FakeShopeeTertiaryContainer,
    background = FakeShopeeBackground,
    surface = FakeShopeeSurface,
    onBackground = FakeShopeeOnSurface,
    onSurface = FakeShopeeOnSurface,
    surfaceVariant = FakeShopeeSurfaceContainerHigh,
    onSurfaceVariant = FakeShopeeSubtext,
    outline = FakeShopeeBorder,
    error = FakeShopeeError,
    onError = Color.White,
    errorContainer = FakeShopeeErrorContainer
)

@Composable
fun FakeShopeeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
