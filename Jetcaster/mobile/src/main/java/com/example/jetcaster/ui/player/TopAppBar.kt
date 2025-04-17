package com.example.jetcaster.ui.player

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.jetcaster.R
import com.example.jetcaster.ui.theme.JetcasterTheme

@Composable
fun TopAppBar(
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        IconButton(onClick = onBackPress) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back)
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onAddToQueue) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = stringResource(R.string.cd_add)
            )
        }
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.cd_more)
            )
        }
    }
}

@Preview
@Composable
fun TopAppBarPreview() {
    JetcasterTheme {
        TopAppBar(
            onBackPress = {},
            onAddToQueue = {},
        )
    }
}

