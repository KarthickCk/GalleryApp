package com.app.gallery.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.gallery.domain.model.Album
import com.app.gallery.domain.model.Media
import com.github.panpf.sketch.AsyncImage

@Composable
fun AlbumComponent(
    modifier: Modifier = Modifier,
    album: Album,
    onItemClick: (Album) -> Unit,
) {

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
        ) {
            AlbumImage(
                album = album,
                onItemClick = onItemClick,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "${album.count} ${album.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun MediaComponent(
    modifier: Modifier = Modifier,
    media: Media,
) {

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
        ) {
            MediaImage(
                media
            )
        }
    }
}

@Composable
fun MediaImage(
    media: Media,
) {

    AsyncImage(
        uri = media.uri.toString(),
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            ),
        contentDescription = media.label,
        contentScale = ContentScale.Crop,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumImage(
    album: Album,
    onItemClick: (Album) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val radius = if (isPressed.value) 32.dp else 16.dp
    val cornerRadius by animateDpAsState(targetValue = radius, label = "cornerRadius")

    AsyncImage(
        uri = album.uri.toString(),
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = { onItemClick(album) },
            ),
        contentDescription = album.label,
        contentScale = ContentScale.Crop,
    )
}