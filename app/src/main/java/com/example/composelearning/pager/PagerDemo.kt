package com.example.composelearning.pager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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

    HorizontalPager(
        count = 30,
        //https://github.com/google/accompanist/issues/849
        // Not sure if i can remove this hardcoded value.
        contentPadding = PaddingValues(horizontal = 160.dp),
        itemSpacing = 8.dp,
        modifier = modifier
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


