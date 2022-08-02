package com.example.composelearning.pager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import kotlin.math.absoluteValue

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerDemo(modifier: Modifier = Modifier) {

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val contentPadding = (maxWidth - 50.dp) / 2
        val center = maxWidth / 2
        val offSet = maxWidth / 5
        val itemSpacing = offSet - 50.dp
        HorizontalPager(
            count = 30,
            contentPadding = PaddingValues(horizontal = contentPadding ),
            modifier = modifier,
            itemSpacing = itemSpacing
        ) { page ->
            Card(
                modifier = Modifier
                    .graphicsLayer {
                        val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
                        // Set the item alpha and scale based on the distance from the center
                        val percentFromCenter = 1.0f - (pageOffset / (5f / 2f))
                        val itemScale = 0.5f + (percentFromCenter * 0.5f).coerceIn(0f, 1f)
                        val opacity = 0.25f + (percentFromCenter * 0.75f).coerceIn(0f, 1f)

                        alpha = opacity
                        scaleY = itemScale
                        scaleX = itemScale
                    }
                    .clip(CircleShape)
                    .size(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)

                )
            }
        }
    }
}


