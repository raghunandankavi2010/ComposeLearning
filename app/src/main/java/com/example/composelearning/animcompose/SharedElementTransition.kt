package com.example.composelearning.animcompose

/*
 * Copyright 2025 Kyriakos Georgiopoulos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R

/**
 * Simple UI model representing a product item that can be displayed and animated
 * across the list and detail screens.
 */
data class Product(
    val id: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val imageRes: Int,
)

/**
 * Represents the high-level navigation state of the product screen.
 */
sealed interface ScreenState {
    data object List : ScreenState
    data class Detail(val product: Product) : ScreenState
}

private const val SCREEN_DURATION = 850
private const val SHARED_DURATION = 1050
private const val DETAILS_DURATION = 900

private val ArcLinearPunchEasing = CubicBezierEasing(0.00f, 0.00f, 0.0f, 1.0f)
private val CinematicEasing = CubicBezierEasing(0.18f, 0.82f, 0.23f, 1.02f)
private val PlayfulEasingTitle = CubicBezierEasing(0.15f, 0.9f, 0.25f, 1.05f)
private val PlayfulEasingSubtitle = CubicBezierEasing(0.20f, 0.8f, 0.25f, 1.05f)

/**
 * Root composable that renders either the product list or a single product detail screen
 * using shared element transitions between them.
 *
 * @param products The list of products to display.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationApi::class)
@Composable
fun ProductScreen(products: List<Product>) {
    var state by remember { mutableStateOf<ScreenState>(ScreenState.List) }
    val listState = rememberLazyListState()

    SharedTransitionLayout {
        val sharedScope = this

        val screenTransition = updateTransition(
            targetState = state, label = "screenTransition"
        )

        val sharedConfig = remember(screenTransition) {
            directionAwareSharedConfig(screenTransition)
        }

        val backgroundColor by screenTransition.animateColor(label = "backgroundColor") { target ->
            when (target) {
                ScreenState.List -> MaterialTheme.colorScheme.surface
                is ScreenState.Detail -> MaterialTheme.colorScheme.surface.copy(alpha = 1f)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(), color = backgroundColor
        ) {
            screenTransition.AnimatedContent(
                transitionSpec = {
                    val forward =
                        initialState is ScreenState.List && targetState is ScreenState.Detail
                    val backward =
                        initialState is ScreenState.Detail && targetState is ScreenState.List

                    when {
                        forward -> {
                            (slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(SCREEN_DURATION, easing = CinematicEasing)
                            ) + fadeIn(
                                tween(
                                    SCREEN_DURATION,
                                    easing = LinearOutSlowInEasing
                                )
                            ) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(SCREEN_DURATION, easing = CinematicEasing)
                            )).togetherWith(
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(
                                        SCREEN_DURATION, easing = CinematicEasing
                                    )
                                ) + fadeOut(
                                    tween(
                                        SCREEN_DURATION - 150, easing = FastOutSlowInEasing
                                    )
                                )
                            )
                        }

                        backward -> {
                            (slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(
                                    SCREEN_DURATION - 80, easing = CinematicEasing
                                )
                            ) + fadeIn(tween(SCREEN_DURATION - 80)))
                                .togetherWith(
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(
                                            SCREEN_DURATION - 80, easing = CinematicEasing
                                        )
                                    ) + fadeOut(tween(SCREEN_DURATION - 150))
                                )
                        }

                        else -> {
                            fadeIn(tween(240))
                                .togetherWith(fadeOut(tween(200)))
                        }
                    }.using(SizeTransform(clip = false))
                },
            ) { target ->
                when (target) {
                    ScreenState.List -> ProductList(
                        products = products,
                        onProductClick = { product ->
                            state = ScreenState.Detail(product)
                        },
                        sharedScope = sharedScope,
                        animatedVisibilityScope = this,
                        sharedConfig = sharedConfig,
                        listState = listState
                    )

                    is ScreenState.Detail -> ProductDetail(
                        product = target.product,
                        onBack = { state = ScreenState.List },
                        sharedScope = sharedScope,
                        animatedVisibilityScope = this,
                        sharedConfig = sharedConfig
                    )
                }
            }
        }
    }
}

/**
 * Shared element configuration that enables transitions in both directions
 * between the list and detail screens.
 */
@Stable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun directionAwareSharedConfig(
    transition: Transition<ScreenState>
): SharedTransitionScope.SharedContentConfig {
    return object : SharedTransitionScope.SharedContentConfig {

        override val SharedTransitionScope.SharedContentState.isEnabled: Boolean
            get() {
                val current = transition.currentState
                val target = transition.targetState
                return (current is ScreenState.List && target is ScreenState.Detail) || (current is ScreenState.Detail && target is ScreenState.List)
            }

        override val shouldKeepEnabledForOngoingAnimation: Boolean
            get() = false
    }
}

/**
 * Linear bounds transform used for shared elements that should move
 * smoothly between their start and end positions.
 */
private fun linearBoundsTransform(
    durationMillis: Int = SHARED_DURATION
): BoundsTransform = BoundsTransform { _, _ ->
    tween(
        durationMillis, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.15f)
    )
}

/**
 * Arc-based bounds transform that moves the shared element along an arc path.
 *
 * @param durationMillis Duration of the animation in milliseconds.
 * @param arcMode The direction of the arc path.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
private fun arcBoundsTransform(
    durationMillis: Int = SHARED_DURATION, arcMode: ArcMode = ArcMode.ArcAbove
): BoundsTransform = BoundsTransform { initial, target ->
    keyframes {
        this.durationMillis = durationMillis
        initial at 0 using arcMode using ArcLinearPunchEasing
        target at durationMillis
    }
}

/**
 * Arc-based bounds transform tuned for the title text shared element.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
private fun arcTitleTransform(
    durationMillis: Int = SHARED_DURATION
): BoundsTransform = BoundsTransform { initial, target ->
    keyframes {
        this.durationMillis = durationMillis
        initial at 0 using ArcMode.ArcAbove using PlayfulEasingTitle
        target at durationMillis
    }
}

/**
 * Arc-based bounds transform tuned for the subtitle text shared element.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
private fun arcSubtitleTransform(
    durationMillis: Int = SHARED_DURATION
): BoundsTransform = BoundsTransform { initial, target ->
    keyframes {
        this.durationMillis = durationMillis
        initial at 0 using ArcMode.ArcBelow using PlayfulEasingSubtitle
        target at durationMillis
    }
}

/**
 * List screen displaying all products in a vertically scrolling list.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedContentScope.ProductList(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedConfig: SharedTransitionScope.SharedContentConfig,
    listState: LazyListState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState
    ) {
        items(products, key = { it.id }) { product ->
            with(sharedScope) {
                ProductListItem(
                    product = product,
                    onClick = { onProductClick(product) },
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedConfig = sharedConfig
                )
            }
        }
    }
}

/**
 * Single product row used in the list screen with shared elements for image and text.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProductListItem(
    product: Product,
    onClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedConfig: SharedTransitionScope.SharedContentConfig
) {
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = product.imageRes),
            contentDescription = product.title,
            modifier = Modifier
                .size(72.dp)
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = "image-${product.id}", config = sharedConfig
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = linearBoundsTransform(),
                )
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.title,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = "title-${product.id}", config = sharedConfig
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = arcTitleTransform()
                    )
                    .skipToLookaheadSize()
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = product.subtitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = "subtitle-${product.id}", config = sharedConfig
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = arcSubtitleTransform()
                    )
                    .skipToLookaheadSize()
            )
        }
    }
}

/**
 * Detail screen for a single product, showing a large header image and description,
 * with shared element transitions from the list card.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedContentScope.ProductDetail(
    product: Product,
    onBack: () -> Unit,
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedConfig: SharedTransitionScope.SharedContentConfig
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {}, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        with(sharedScope) {
            val headerShape = RoundedCornerShape(bottomStart = 42.dp, bottomEnd = 42.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            key = "imageBounds-${product.id}", config = sharedConfig
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                            contentScale = ContentScale.Fit, alignment = Alignment.Center
                        ),
                        boundsTransform = arcBoundsTransform(),
                    ), contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.title,
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.Center)
                        .offset(y = (-40).dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(
                                key = "image-${product.id}", config = sharedConfig
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = linearBoundsTransform(),
                        )
                        .graphicsLayer {
                            shape = headerShape
                            clip = !isTransitionActive
                        },
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.6f to Color.Black.copy(alpha = 0.18f),
                                1f to Color.Black.copy(alpha = 0.58f)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = product.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = "title-${product.id}", config = sharedConfig
                                ),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = arcTitleTransform()
                            )
                            .skipToLookaheadSize()
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = product.subtitle,
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = "subtitle-${product.id}", config = sharedConfig
                                ),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = arcSubtitleTransform()
                            )
                            .skipToLookaheadSize()
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
                .animateEnterExit(
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = DETAILS_DURATION,
                            delayMillis = 120,
                            easing = LinearOutSlowInEasing
                        )
                    ) + slideInVertically(
                        animationSpec = tween(
                            durationMillis = DETAILS_DURATION,
                            delayMillis = 120,
                            easing = CinematicEasing
                        )
                    ) { it / 5 }, exit = fadeOut(
                        animationSpec = tween(260)
                    ) + slideOutVertically(
                        animationSpec = tween(260)
                    ) { it / 5 }), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Details", style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = loremLong, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp
            )
        }
    }
}

/**
 * Placeholder long-form description used for the product details section.
 */
private val loremLong = """
    This is where you put your long-form product description. 
    In a real app this would come from your backend or local DB. 
    The important part for our transition is that the header image 
    and title feel physically connected to the list card the user tapped.
""".trimIndent()

/**
 * Sample product data for previews or manual testing.
 */
val sampleProducts: List<Product> = listOf(
    Product(
        id = "espresso",
        title = "Espresso",
        subtitle = "Strong, short, and bold – the purest coffee shot.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "cappuccino",
        title = "Cappuccino",
        subtitle = "Velvety milk foam over a rich espresso base.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "latte",
        title = "Caffè Latte",
        subtitle = "Smooth and milky, perfect for a slow morning.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "mocha",
        title = "Mocha",
        subtitle = "Chocolate meets espresso – a sweet classic.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "iced",
        title = "Iced Coffee",
        subtitle = "Cold, refreshing, and perfect for summer days.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "cookies",
        title = "Fresh Cookies",
        subtitle = "Warm, gooey cookies – perfect with any drink.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "flat_white",
        title = "Flat White",
        subtitle = "Silky microfoam over a rich double espresso.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "filter",
        title = "Filter Coffee",
        subtitle = "Slow-brewed, clean and bright in flavor.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "toast",
        title = "Buttered Toast",
        subtitle = "Golden, crispy slices with melted butter.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "tea",
        title = "Herbal Tea",
        subtitle = "Calming blend of herbs, no caffeine, all comfort.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "cake",
        title = "Slice of Cake",
        subtitle = "Rich, moist cake – your afternoon treat.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "cupcake",
        title = "Cupcake",
        subtitle = "Small, sweet, and topped with creamy frosting.",
        imageRes = R.drawable.ic_launcher_background,
    ), Product(
        id = "takeaway",
        title = "Takeaway Coffee",
        subtitle = "Your favorite brew, ready to go.",
        imageRes = R.drawable.ic_launcher_background,
    )
)