package it.unibo.trace.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import it.unibo.trace.R.font

val SpaceGrotesk = FontFamily(
    Font(font.space_grotesk, FontWeight.Medium)
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = SpaceGrotesk),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = SpaceGrotesk),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = SpaceGrotesk),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = SpaceGrotesk),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = SpaceGrotesk),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = SpaceGrotesk),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = SpaceGrotesk),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = SpaceGrotesk),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = SpaceGrotesk),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = SpaceGrotesk),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = SpaceGrotesk),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = SpaceGrotesk),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = SpaceGrotesk),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = SpaceGrotesk),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = SpaceGrotesk)
)
