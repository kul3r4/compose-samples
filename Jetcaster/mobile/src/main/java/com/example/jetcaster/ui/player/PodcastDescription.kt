package com.example.jetcaster.ui.player

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun PodcastDescription(
    title: String,
    podcastName: String,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
) {
    Text(
        text = title,
        style = titleTextStyle,
        maxLines = 1,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.basicMarquee()
    )
    Text(
        text = podcastName,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
    )
}
