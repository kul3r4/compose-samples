package com.example.jetcaster.ui.player

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import com.example.jetcaster.ui.LocalAnimatedVisibilityScope
import com.example.jetcaster.ui.LocalSharedTransitionScope
import com.example.jetcaster.util.isBookPosture
import com.example.jetcaster.util.isSeparatingPosture
import com.example.jetcaster.util.isTableTopPosture
import com.example.jetcaster.util.verticalGradientScrim
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.VerticalTwoPaneStrategy


@Composable
fun PlayerContent(
    uiState: PlayerUiState,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier,
) {
    val foldingFeature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()

    // Use a two pane layout if there is a fold impacting layout (meaning it is separating
    // or non-flat) or if we have a large enough width to show both.
    if (
        windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
        isBookPosture(foldingFeature) ||
        isTableTopPosture(foldingFeature) ||
        isSeparatingPosture(foldingFeature)
    ) {
        // Determine if we are going to be using a vertical strategy (as if laying out
        // both sides in a column). We want to do so if we are in a tabletop posture,
        // or we have an impactful horizontal fold. Otherwise, we'll use a horizontal strategy.
        val usingVerticalStrategy =
            isTableTopPosture(foldingFeature) ||
                    (
                            isSeparatingPosture(foldingFeature) &&
                                    foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL
                            )

        if (usingVerticalStrategy) {
            TwoPane(
                first = {
                    PlayerContentTableTopTop(
                        uiState = uiState,
                    )
                },
                second = {
                    PlayerContentTableTopBottom(
                        uiState = uiState,
                        onBackPress = onBackPress,
                        onAddToQueue = onAddToQueue,
                        playerControlActions = playerControlActions,
                    )
                },
                strategy = VerticalTwoPaneStrategy(splitFraction = 0.5f),
                displayFeatures = displayFeatures,
                modifier = modifier,
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalGradientScrim(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                        startYPercentage = 1f,
                        endYPercentage = 0f
                    )
                    .systemBarsPadding()
                    .padding(horizontal = 8.dp)
            ) {
                TopAppBar(
                    onBackPress = onBackPress,
                    onAddToQueue = onAddToQueue,
                )
                TwoPane(
                    first = {
                        PlayerContentBookStart(uiState = uiState)
                    },
                    second = {
                        PlayerContentBookEnd(
                            uiState = uiState,
                            playerControlActions = playerControlActions,
                        )
                    },
                    strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f),
                    displayFeatures = displayFeatures
                )
            }
        }
    } else {
        PlayerContentRegular(
            uiState = uiState,
            onBackPress = onBackPress,
            onAddToQueue = onAddToQueue,
            playerControlActions = playerControlActions,
            modifier = modifier,
        )
    }
}

/**
 * The UI for the top pane of a tabletop layout.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlayerContentRegular(
    uiState: PlayerUiState,
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier,
) {
    val playerEpisode = uiState.episodePlayerState
    val currentEpisode = playerEpisode.currentEpisode ?: return

    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No SharedElementScope found")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalGradientScrim(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                startYPercentage = 1f,
                endYPercentage = 0f
            )
            .systemBarsPadding()
            .padding(horizontal = 8.dp)
    ) {
        TopAppBar(
            onBackPress = onBackPress,
            onAddToQueue = onAddToQueue,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            with(sharedTransitionScope) {
                with(animatedVisibilityScope) {
                    PlayerImage(
                        podcastImageUrl = currentEpisode.podcastImageUrl,
                        modifier = Modifier
                            .weight(10f)
                            .animateEnterExit(
                                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)),
                                exit = fadeOut()
                            ),
                        imageModifier = Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(
                                key = currentEpisode.title
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = OverlayClip(MaterialTheme.shapes.medium)
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                PodcastDescription(currentEpisode.title, currentEpisode.podcastName)
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(10f)
                ) {
                    PlayerSlider(
                        timeElapsed = playerEpisode.timeElapsed,
                        episodeDuration = currentEpisode.duration,
                        onSeekingStarted = playerControlActions.onSeekingStarted,
                        onSeekingFinished = playerControlActions.onSeekingFinished
                    )
                    PlayerButtons(
                        hasNext = playerEpisode.queue.isNotEmpty(),
                        isPlaying = playerEpisode.isPlaying,
                        onPlayPress = playerControlActions.onPlayPress,
                        onPausePress = playerControlActions.onPausePress,
                        onAdvanceBy = playerControlActions.onAdvanceBy,
                        onRewindBy = playerControlActions.onRewindBy,
                        onNext = playerControlActions.onNext,
                        onPrevious = playerControlActions.onPrevious,
                        Modifier.padding(vertical = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * The UI for the top pane of a tabletop layout.
 */
@Composable
fun PlayerContentTableTopTop(
    uiState: PlayerUiState,
    modifier: Modifier = Modifier,
) {
    // Content for the top part of the screen
    val episode = uiState.episodePlayerState.currentEpisode ?: return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalGradientScrim(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                startYPercentage = 1f,
                endYPercentage = 0f
            )
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerImage(episode.podcastImageUrl)
    }
}

/**
 * The UI for the bottom pane of a tabletop layout.
 */
@Composable
fun PlayerContentTableTopBottom(
    uiState: PlayerUiState,
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier,
) {
    val episodePlayerState = uiState.episodePlayerState
    val episode = uiState.episodePlayerState.currentEpisode ?: return
    // Content for the table part of the screen
    Column(
        modifier = modifier
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            onBackPress = onBackPress,
            onAddToQueue = onAddToQueue,
        )
        PodcastDescription(
            title = episode.title,
            podcastName = episode.podcastName,
            titleTextStyle = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.weight(0.5f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(10f)
        ) {
            PlayerButtons(
                hasNext = episodePlayerState.queue.isNotEmpty(),
                isPlaying = episodePlayerState.isPlaying,
                onPlayPress = playerControlActions.onPlayPress,
                onPausePress = playerControlActions.onPausePress,
                playerButtonSize = 92.dp,
                onAdvanceBy = playerControlActions.onAdvanceBy,
                onRewindBy = playerControlActions.onRewindBy,
                onNext = playerControlActions.onNext,
                onPrevious = playerControlActions.onPrevious,
                modifier = Modifier.padding(top = 8.dp)
            )
            PlayerSlider(
                timeElapsed = episodePlayerState.timeElapsed,
                episodeDuration = episode.duration,
                onSeekingStarted = playerControlActions.onSeekingStarted,
                onSeekingFinished = playerControlActions.onSeekingFinished
            )
        }
    }
}

/**
 * The UI for the start pane of a book layout.
 */
@Composable
private fun PlayerContentBookStart(
    uiState: PlayerUiState,
    modifier: Modifier = Modifier,
) {
    val episode = uiState.episodePlayerState.currentEpisode ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                vertical = 40.dp,
                horizontal = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PodcastInformation(
            title = episode.title,
            name = episode.podcastName,
            summary = episode.summary,
        )
    }
}

/**
 * The UI for the end pane of a book layout.
 */
@Composable
private fun PlayerContentBookEnd(
    uiState: PlayerUiState,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier,
) {
    val episodePlayerState = uiState.episodePlayerState
    val episode = episodePlayerState.currentEpisode ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        PlayerImage(
            podcastImageUrl = episode.podcastImageUrl,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .weight(1f)
        )
        PlayerSlider(
            timeElapsed = episodePlayerState.timeElapsed,
            episodeDuration = episode.duration,
            onSeekingStarted = playerControlActions.onSeekingStarted,
            onSeekingFinished = playerControlActions.onSeekingFinished,
        )
        PlayerButtons(
            hasNext = episodePlayerState.queue.isNotEmpty(),
            isPlaying = episodePlayerState.isPlaying,
            onPlayPress = playerControlActions.onPlayPress,
            onPausePress = playerControlActions.onPausePress,
            onAdvanceBy = playerControlActions.onAdvanceBy,
            onRewindBy = playerControlActions.onRewindBy,
            onNext = playerControlActions.onNext,
            onPrevious = playerControlActions.onPrevious,
            Modifier.padding(vertical = 8.dp)
        )
    }
}
