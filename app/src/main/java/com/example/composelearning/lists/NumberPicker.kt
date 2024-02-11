package com.example.composelearning.lists


import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Answering
 * https://stackoverflow.com/questions/77975928/how-to-implement-this-type-of-number-picker-in-jetpack-compose/77976047#77976047
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberPicker(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val contentPadding = (maxWidth - 50.dp) / 2
        val offSet = maxWidth / 5
        val itemSpacing = offSet - 50.dp
        val pagerState = rememberPagerState(pageCount = {
            100
        })

        val context = LocalContext.current

        LaunchedEffect(pagerState) {
            // Collect from the a snapshotFlow reading the currentPage
            snapshotFlow { pagerState.settledPage }.collect { page ->
                Toast.makeText(context,"$page",Toast.LENGTH_SHORT).show()
            }
        }

        val scope = rememberCoroutineScope()

        val mutableInteractionSource = remember {
            MutableInteractionSource()
        }

        CenterCircle(
            modifier = modifier
                .size(50.dp)
                .align(Alignment.Center),
            fillColor = Color.Magenta,
            strokeWidth = 2.dp
        )

        HorizontalPager(
            modifier = modifier,
            state = pagerState,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(0)
            ),
            contentPadding = PaddingValues(horizontal = contentPadding),
            pageSpacing = itemSpacing,
        ) { page ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .graphicsLayer {
                        val pageOffset = ((pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction).absoluteValue
                        // Set the item alpha based on the distance from the center
                        val percentFromCenter = 1.0f - (pageOffset / (5f / 2f))
                        val opacity = 0.25f + (percentFromCenter * 0.75f).coerceIn(0f, 1f)

                        alpha = opacity
                        clip = true
                    }
                    .clickable(
                        interactionSource = mutableInteractionSource,
                        indication = null,
                        enabled = true,
                    ) {
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }) {
                Text(
                    text = "$page",
                    color = Color.Black,
                    modifier = Modifier
                        .size(50.dp)
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

