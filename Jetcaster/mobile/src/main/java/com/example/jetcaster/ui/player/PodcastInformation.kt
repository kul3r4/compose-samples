package com.example.jetcaster.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jetcaster.designsystem.component.HtmlTextContainer
import java.time.Duration

@Composable
fun PodcastInformation(
    title: String,
    name: String,
    summary: String,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    nameTextStyle: TextStyle = MaterialTheme.typography.displaySmall,
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = name,
            style = nameTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = title,
            style = titleTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        HtmlTextContainer(text = summary) {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current
            )
        }
    }
}

fun Duration.formatString(): String {
    val minutes = this.toMinutes().toString().padStart(2, '0')
    val secondsLeft = (this.toSeconds() % 60).toString().padStart(2, '0')
    return "$minutes:$secondsLeft"
}