package it.unibo.trace.ui.composable.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import it.unibo.trace.data.supabase.entities.TodoItem

/**
 * A card component representing a single Todo item.
 *
 * @param item The [TodoItem] to display.
 * @param onToggle Callback invoked when the user clicks on the card (to complete the task).
 * @param isCompleted Whether the task is currently marked as completed (pending deletion).
 * @param modifier Optional [Modifier] for the card.
 */
@Composable
fun TodoCard(
    item: TodoItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    TraceCard(
        modifier = modifier.alpha(if (isCompleted) 0.5f else 1f),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isCompleted) "Task Completed" else "Complete Task",
                tint = if (isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
