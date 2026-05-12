package it.unibo.trace.ui.composable.image

import androidx.compose.ui.res.stringResource
import it.unibo.trace.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage

@Composable
fun ProfileAvatar(
    url: String,
    imageLoader: ImageLoader,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(240.dp)
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        SmallFloatingActionButton(
            onClick = onEditClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.AddAPhoto, contentDescription = stringResource(R.string.update_photo_desc))
        }
    }
}
