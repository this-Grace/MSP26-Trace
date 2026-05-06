package it.unibo.trace.ui.composable.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun SocialSignInButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    iconTint: Color = MaterialTheme.colorScheme.onBackground
) {
    TraceButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        isLoading = isLoading,
        outlined = true,
        icon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = iconTint
            )
        }
    )
}