package com.example.composelearning.pager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
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

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val totalPadding = (screenWidth/2 - (50.dp))

    val totalNumberOfItem  = 5

    println(totalPadding)

    HorizontalPager(
        count = 10,
        contentPadding = PaddingValues(horizontal = totalPadding, vertical = 0.dp),
        modifier = modifier
    ) { page ->

        Card(
            modifier = Modifier
                .graphicsLayer {
                    val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue

                    // Set the item alpha and scale based on the distance from the center
                    val percentFromCenter = 1.0f - (pageOffset / (totalNumberOfItem/2f))
                    val itemScale = 0.5f + (percentFromCenter * 0.5f).coerceIn(0f, 1f)
                    val opacity = 0.25f + (percentFromCenter * 0.75f).coerceIn(0f, 1f)

                    alpha = opacity
                    scaleY = itemScale
                    scaleX= itemScale

                }.clip(CircleShape)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}