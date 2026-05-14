package com.example.composelearning.tutorial.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.tutorial.presentation.TutorialEvent
import com.example.composelearning.tutorial.presentation.TutorialUiState
import com.example.composelearning.tutorial.presentation.TutorialViewModel
import com.example.composelearning.tutorial.ui.components.FeatureCardItem
import com.example.composelearning.tutorial.ui.overlay.CalloutCard
import com.example.composelearning.tutorial.ui.overlay.LocalSpotlightController
import com.example.composelearning.tutorial.ui.overlay.SpotlightController
import com.example.composelearning.tutorial.ui.overlay.SpotlightScrim
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    modifier: Modifier = Modifier,
    viewModel: TutorialViewModel = viewModel(factory = TutorialViewModel.factory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val controller = remember { SpotlightController() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Your feed") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            if (state.tutorial is TutorialUiState.TutorialState.Idle) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onEvent(TutorialEvent.Start) },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    text = { Text("Show tutorial") },
                )
            }
        },
    ) { padding ->
        CompositionLocalProvider(LocalSpotlightController provides controller) {
            TutorialContent(
                state = state,
                controller = controller,
                onEvent = viewModel::onEvent,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun TutorialContent(
    state: TutorialUiState,
    controller: SpotlightController,
    onEvent: (TutorialEvent) -> Unit,
    contentPadding: PaddingValues,
) {
    val listState = rememberLazyListState()
    val cornerRadiusPx = with(LocalDensity.current) { 20.dp.toPx() }

    var overlayCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    val tutorialRunning = state.tutorial is TutorialUiState.TutorialState.Running
    LaunchedEffect(tutorialRunning) {
        if (tutorialRunning) controller.activate() else controller.deactivate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .onGloballyPositioned {
                overlayCoords = it
                overlaySize = it.size
            },
    ) {
        when (val cards = state.cards) {
            TutorialUiState.CardsState.Loading -> LoadingState()
            is TutorialUiState.CardsState.Ready -> {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(cards.cards, key = { it.id }) { card ->
                        FeatureCardItem(card = card)
                    }
                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }

        val tutorial = state.tutorial
        if (tutorial is TutorialUiState.TutorialState.Running) {
            val step = tutorial.currentStep
            var localTargetRect by remember { mutableStateOf<Rect?>(null) }

            LaunchedEffect(tutorial.currentIndex, overlayCoords) {
                localTargetRect = null
                val cardIndex = step.cardIndex
                if (cardIndex != null) {
                    listState.animateScrollToItem(cardIndex)
                }
                val windowRect = snapshotFlow { controller.boundsOf(step.targetKey) }
                    .filterNotNull()
                    .first()
                val coords = overlayCoords ?: return@LaunchedEffect
                val topLeftLocal = coords.windowToLocal(windowRect.topLeft)
                localTargetRect = Rect(topLeftLocal, windowRect.size)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(tutorial.currentIndex) { /* swallow taps on scrim */ },
            ) {
                SpotlightScrim(
                    targetRect = localTargetRect,
                    cornerRadiusPx = cornerRadiusPx,
                    scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f),
                )
                CalloutCard(
                    title = step.title,
                    description = step.description,
                    stepNumber = tutorial.currentIndex + 1,
                    totalSteps = tutorial.steps.size,
                    targetRect = localTargetRect,
                    overlaySizePx = overlaySize,
                    onNext = { onEvent(TutorialEvent.Next) },
                    onSkip = { onEvent(TutorialEvent.Skip) },
                    isLast = tutorial.isLast,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Loading your feed…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}