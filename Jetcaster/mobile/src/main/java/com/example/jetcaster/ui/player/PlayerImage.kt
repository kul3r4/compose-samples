package com.example.jetcaster.ui.player

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.jetcaster.designsystem.component.PodcastImage

@Composable
fun PlayerImage(
    podcastImageUrl: String,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
) {
    PodcastImage(
        podcastImageUrl = podcastImageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .sizeIn(maxWidth = 500.dp, maxHeight = 500.dp)
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium),
        imageModifier = imageModifier
    )
}
