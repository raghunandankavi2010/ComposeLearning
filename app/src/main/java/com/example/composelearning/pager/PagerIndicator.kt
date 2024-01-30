package com.example.composelearning.pager


import android.widget.Space
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import kotlin.math.absoluteValue


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerIndicatorDemo() {
    var currentPage by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(pageCount = {
        5
    })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    // Your pager content here
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { iteration ->
                    val activePage = pagerState.currentPage == iteration
                    val color = if (pagerState.currentPage == iteration) Color.Red else Color.Red.copy(alpha = 0.5f)
                    if(activePage) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(color)
                                .height(8.dp)
                                .width(24.dp)

                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)

                        )
                    }
                }
            }
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerIndicator(
    pagerState: PagerState,
    count: Int,
    activeDotWidth: Dp,
    dotWidth: Dp,
    circleSpacing: Dp,
    activeLineWidth: Dp
) {
    val spacing = with(LocalDensity.current) { circleSpacing.toPx() }
    val dotWidthPx = with(LocalDensity.current) { dotWidth.toPx() }
    val activeDotWidthPx = with(LocalDensity.current) { activeDotWidth.toPx() }
    val activeLineWidthPx = with(LocalDensity.current) { activeLineWidth.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var x = 0f
        val y = center.y

        repeat(count) { i ->
            val posOffset = pagerState.calculateCurrentOffsetForPage(i)
            //val posOffset = pagerState.getOffsetFractionForPage(pagerState.currentPage)
            val dotOffset = posOffset.absoluteValue % 1
            val current = posOffset.toInt()

            val factor = dotOffset * (activeDotWidthPx - dotWidthPx)

            val calculatedWidth = when {
                i == current -> activeDotWidthPx - factor
                i - 1 == current || (i == 0 && posOffset > count - 1) -> dotWidthPx + factor
                else -> dotWidthPx
            }

            drawRoundRect(
                color = Color.Red,
                topLeft = Offset(x, y - dotWidthPx / 2),
                size = Size(calculatedWidth, dotWidthPx),
                style = Fill,
                alpha = 1.0f,
                colorFilter = null,
                blendMode = DefaultBlendMode,
                cornerRadius = CornerRadius(activeLineWidthPx / 2)
            )
            x += calculatedWidth + spacing
        }
    }
}

// To get scrolled offset from snap position
@OptIn(ExperimentalFoundationApi::class)
fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage - page) + currentPageOffsetFraction
}
