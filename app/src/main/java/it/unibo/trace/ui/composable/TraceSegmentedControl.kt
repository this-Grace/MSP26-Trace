package it.unibo.trace.ui.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A generic segmented control component for selecting between multiple options.
 */
@Composable
fun <T> TraceSegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    iconProvider: (T) -> ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val selected = option == selectedOption
                val backgroundColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "bgColor"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "contentColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(backgroundColor)
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = iconProvider(option),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = contentColor
                        )
                        Text(
                            text = labelProvider(option),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
