/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetcaster.ui.player

/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import com.example.jetcaster.R
import com.example.jetcaster.ui.components.SettingsButtons
import com.google.android.horologist.audio.ui.VolumeUiState
import com.google.android.horologist.audio.ui.VolumeViewModel
import com.google.android.horologist.audio.ui.volumeRotaryBehavior
import com.google.android.horologist.images.coil.CoilPaintable
import com.google.android.horologist.media.ui.components.PodcastControlButtons
import com.google.android.horologist.media.ui.components.background.ArtworkColorBackground
import com.google.android.horologist.media.ui.components.controls.SeekButtonIncrement
import com.google.android.horologist.media.ui.components.display.LoadingMediaDisplay
import com.google.android.horologist.media.ui.components.display.TextMediaDisplay
import com.google.android.horologist.media.ui.screens.player.PlayerScreen
import java.time.Duration

@Composable
fun PlayerScreen(
    volumeViewModel: VolumeViewModel,
    onVolumeClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerScreenViewModel: PlayerViewModel = hiltViewModel(),
) {
    val volumeUiState by volumeViewModel.volumeUiState.collectAsStateWithLifecycle()

    PlayerScreen(
        playerScreenViewModel = playerScreenViewModel,
        volumeUiState = volumeUiState,
        onVolumeClick = onVolumeClick,
        onUpdateVolume = { newVolume -> volumeViewModel.setVolume(newVolume) },
        modifier = modifier
    )
}

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalWearMaterialApi::class)
@Composable
private fun PlayerScreen(
    playerScreenViewModel: PlayerViewModel,
    volumeUiState: VolumeUiState,
    onVolumeClick: () -> Unit,
    onUpdateVolume: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by playerScreenViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val state = uiState) {
        PlayerScreenUiState.Loading -> LoadingMediaDisplay(modifier)
        PlayerScreenUiState.Empty -> {
            PlayerScreen(
                mediaDisplay = {
                    TextMediaDisplay(
                        title = stringResource(R.string.nothing_playing),
                        subtitle = ""
                    )
                },
                controlButtons = {
                    PodcastControlButtons(
                        onPlayButtonClick = playerScreenViewModel::onPlay,
                        onPauseButtonClick = playerScreenViewModel::onPause,
                        playPauseButtonEnabled = false,
                        playing = false,
                        onSeekBackButtonClick = playerScreenViewModel::onRewindBy,
                        seekBackButtonEnabled = false,
                        onSeekForwardButtonClick = playerScreenViewModel::onAdvanceBy,
                        seekForwardButtonEnabled = false
                    )
                },
                buttons = {
                    SettingsButtons(
                        volumeUiState = volumeUiState,
                        onVolumeClick = onVolumeClick,
                        playerUiState = PlayerUiState(),
                        onPlaybackSpeedChange = playerScreenViewModel::onPlaybackSpeedChange,
                        enabled = false,
                    )
                },
            )
        }

        is PlayerScreenUiState.Ready -> {
            // When screen is ready, episode is always not null, however EpisodePlayerState may
            // return a null episode
            val episode = state.playerState.episodePlayerState.currentEpisode

            val exoPlayer = rememberPlayer(context)

            DisposableEffect(exoPlayer, episode) {
                episode?.mediaUrls?.let { exoPlayer.setMediaItems(it.map { MediaItem.fromUri(it) }) }
                val mediaSession = MediaSession.Builder(context, exoPlayer).build()

                exoPlayer.prepare()

                onDispose {
                    mediaSession.release()
                    exoPlayer.release()
                }
            }
            Box {
                PlayerSurface(
                    player = exoPlayer,
                    modifier = Modifier.resizeWithContentScale(
                        contentScale = ContentScale.Fit,
                        sourceSizeDp = null
                    )
                )
                PlayerScreen(
                    mediaDisplay = {

                        if (episode != null && episode.title.isNotEmpty()) {
                            TextMediaDisplay(
                                title = episode.podcastName, subtitle = episode.title
                            )
                        } else {
                            TextMediaDisplay(
                                title = stringResource(R.string.nothing_playing), subtitle = ""
                            )
                        }
                    },
                    controlButtons = {
                        PodcastControlButtons(
                            onPlayButtonClick = ({
                                playerScreenViewModel.onPlay()
                                exoPlayer.play()
                            }),
                            onPauseButtonClick = ({
                                playerScreenViewModel.onPause()
                                exoPlayer.pause()
                            }),
                            playPauseButtonEnabled = true,
                            playing = state.playerState.episodePlayerState.isPlaying,
                            onSeekBackButtonClick = ({
                                playerScreenViewModel.onRewindBy()
                                exoPlayer.seekBack()
                            }),
                            seekBackButtonEnabled = true,
                            onSeekForwardButtonClick = ({
                                playerScreenViewModel.onAdvanceBy()
                                exoPlayer.seekForward()
                            }),
                            seekForwardButtonEnabled = true,
                            seekBackButtonIncrement = SeekButtonIncrement.Ten,
                            seekForwardButtonIncrement = SeekButtonIncrement.Ten,
                            trackPositionUiModel = state.playerState.trackPositionUiModel
                        )
                    },
                    buttons = {
                        SettingsButtons(
                            volumeUiState = volumeUiState,
                            onVolumeClick = onVolumeClick,
                            playerUiState = state.playerState,
                            onPlaybackSpeedChange = ({
                                playerScreenViewModel.onPlaybackSpeedChange()
                                if (state.playerState.episodePlayerState.playbackSpeed == Duration.ofSeconds(
                                        1
                                    )
                                )
                                    exoPlayer.setPlaybackSpeed(1.5F)
                                else if (state.playerState.episodePlayerState.playbackSpeed == Duration.ofMillis(
                                        1500
                                    )
                                )
                                    exoPlayer.setPlaybackSpeed(2.0F)
                                else if (state.playerState.episodePlayerState.playbackSpeed == Duration.ofSeconds(
                                        2
                                    )
                                )
                                    exoPlayer.setPlaybackSpeed(1.0F)
                            }),
                            enabled = true,
                        )
                    },
                    modifier = modifier.rotaryScrollable(
                        volumeRotaryBehavior(
                            volumeUiStateProvider = { volumeUiState },
                            onRotaryVolumeInput = { onUpdateVolume },
                        ),
                        focusRequester = rememberActiveFocusRequester(),
                    ),
                    background = {
                        ArtworkColorBackground(
                            paintable = episode?.let { CoilPaintable(episode.podcastImageUrl) },
                            defaultColor = MaterialTheme.colors.primary,
                            modifier = Modifier.fillMaxSize(),
                        )
                    })
            }
        }
    }
}


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun rememberPlayer(
    context: Context,
) = remember {
    ExoPlayer.Builder(context).setSeekForwardIncrementMs(10000).setSeekBackIncrementMs(10000)
        .setMediaSourceFactory(
            ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
        ).setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
        }
}
