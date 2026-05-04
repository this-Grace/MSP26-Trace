package it.unibo.trace.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.unibo.trace.ui.theme.AppTheme

/**
 * A selector for the application theme, using a segmented control.
 */
@Composable
fun ThemeSelector(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    TraceSegmentedControl(
        options = AppTheme.entries,
        selectedOption = selectedTheme,
        onOptionSelected = onThemeSelected,
        labelProvider = { theme ->
            when (theme) {
                AppTheme.LIGHT -> "Light"
                AppTheme.DARK -> "Dark"
                AppTheme.SYSTEM -> "System"
            }
        },
        iconProvider = { theme ->
            when (theme) {
                AppTheme.LIGHT -> Icons.Rounded.LightMode
                AppTheme.DARK -> Icons.Rounded.Brightness4
                AppTheme.SYSTEM -> Icons.Rounded.BrightnessAuto
            }
        },
        modifier = modifier
    )
}
