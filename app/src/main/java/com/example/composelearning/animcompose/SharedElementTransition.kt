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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size

/**
 * Simple UI model representing a product item that can be displayed and animated
 * across the list and detail screens.
 *
 * `imageUrl` points to a single remote image; the list thumbnail and the detail header reuse the
 * SAME URL so Coil can serve both from a shared bitmap. See [rememberProductImageRequest] for the
 * caching contract that makes this work without a flicker during the shared element transition.
 */
data class Product(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
)

/**
 * Build a Coil [ImageRequest] for a product, set up so that the list thumbnail and the detail
 * header behave like one image as far as the cache is concerned.
 *
 * Why a single request per product matters for shared element transitions:
 *   1. By default, Coil sizes the request to the layout's measured size — so a 72.dp thumbnail and
 *      a 240.dp header would issue two separate network requests and live as two distinct memory
 *      cache entries. Tapping a list row would then "snap" to a half-loaded detail image and
 *      crossfade in, flickering on top of the shared element animation.
 *   2. Pinning [Size.ORIGINAL] makes both call sites ask for the same pixel buffer. One download.
 *   3. Setting both [ImageRequest.Builder.memoryCacheKey] and
 *      [ImageRequest.Builder.placeholderMemoryCacheKey] to a stable per-product key means the
 *      detail screen's `AsyncImage` finds the list-loaded bitmap in cache instantly when the
 *      transition lands — there is no "loading" frame to flicker.
 *   4. Crossfade is disabled (`crossfade(false)`). With a memory-cache hit the destination would
 *      otherwise fade in from transparent, which competes visually with the shared-element layer
 *      that's still animating on top.
 *
 * How the transition itself plays out:
 *   • While the transition is in flight, the shared content is drawn into an overlay above both
 *     screens. The overlay uses the SOURCE composable's painter (so the bitmap that's already
 *     loaded in the list does the heavy lifting during the morph). The destination's
 *     `AsyncImage` is composed underneath but its content only matters once the transition
 *     completes — and by then the cache key lookup returns the same bitmap synchronously.
 *   • Net result: the user sees a single image growing from 72dp → 240dp. No second download.
 */
@Composable
private fun rememberProductImageRequest(product: Product): ImageRequest {
    val context = LocalContext.current
    return remember(product.id, product.imageUrl) {
        ImageRequest.Builder(context)
            .data(product.imageUrl)
            // Force the same pixel buffer for every consumer of this product's image.
            .size(Size.ORIGINAL)
            // Shared cache key — list and detail end up looking up the same memory entry.
            .memoryCacheKey(product.id)
            .placeholderMemoryCacheKey(product.id)
            // Enable both caches explicitly so a cache miss doesn't silently do nothing.
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // Disable Coil's own crossfade — it interferes with the shared element overlay.
            .crossfade(false)
            .build()
    }
}

/**
 * Wrapper around [AsyncImage] that always paints SOMETHING.
 *
 * Default `AsyncImage` draws an empty rectangle while the image is loading and on error — which
 * looks like "nothing happened" on screen. The placeholder/error painters here keep a visible
 * surface in place so the layout doesn't appear empty.
 *
 * On the error path we also log the underlying throwable so failures (e.g. emulator without
 * internet, DNS issues, certificate problems) show up in logcat under the `ProductImage` tag.
 */
@Composable
private fun ProductImage(
    request: ImageRequest,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val placeholderPainter: Painter = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    val errorPainter: Painter = ColorPainter(MaterialTheme.colorScheme.errorContainer)
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = placeholderPainter,
        error = errorPainter,
        fallback = placeholderPainter,
        onError = { state ->
            android.util.Log.e(
                "ProductImage",
                "Failed to load ${request.data}",
                state.result.throwable,
            )
        },
    )
}

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
    val imageRequest = rememberProductImageRequest(product)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // List thumbnail. Uses the same Coil request as the detail header — sharing the memory
        // cache key avoids a second download and the corresponding flash of a placeholder while
        // the shared element transition is running. The 16.dp rounded clip is mirrored into
        // `clipInOverlayDuringTransition` so the overlay drawn during the transition is also
        // rounded (without it the image pops "rounded → square → rounded").
        ProductImage(
            request = imageRequest,
            contentDescription = product.title,
            modifier = Modifier
                .size(72.dp)
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = "image-${product.id}", config = sharedConfig
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = linearBoundsTransform(),
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(16.dp)),
                )
                .clip(RoundedCornerShape(16.dp)),
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

        val imageRequest = rememberProductImageRequest(product)
        with(sharedScope) {
            val headerShape = RoundedCornerShape(bottomStart = 42.dp, bottomEnd = 42.dp)
            val imageShape = RoundedCornerShape(16.dp)

            // The rounded bottom corners belong to the whole header card, not the image. We clip
            // the outer Box (which also has `sharedBounds`) so the gradient and the text inside
            // are clipped along with the image. `clipInOverlayDuringTransition = OverlayClip(...)`
            // makes the overlay drawn during the transition respect the same shape — otherwise
            // the corners only appear after the transition finishes.
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
                        clipInOverlayDuringTransition = OverlayClip(headerShape),
                    )
                    .clip(headerShape),
                contentAlignment = Alignment.Center
            ) {
                // Detail header image — same Coil request as the list thumbnail, so the bitmap is
                // already in memory cache when this composition resolves (no second download, no
                // crossfade-into-placeholder competing with the shared element overlay).
                ProductImage(
                    request = imageRequest,
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
                            clipInOverlayDuringTransition = OverlayClip(imageShape),
                        )
                        .clip(imageShape),
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
/**
 * Sample products. Image URLs point at specific photos on Unsplash's CDN — each one was picked to
 * match the product it represents (a real espresso photo for espresso, etc.). The URLs use
 * Unsplash's image-resize params (`w`, `q`, `auto=format`, `fit=crop`) so the CDN returns a
 * reasonably-sized cropped image rather than the multi-megabyte original.
 *
 * If a particular URL ever 404s (Unsplash photos can be taken down by their authors), the
 * `ProductImage` wrapper will show the `errorContainer` placeholder and log the failure under the
 * `ProductImage` tag in logcat — easy to spot and swap.
 */
val sampleProducts: List<Product> = listOf(
    Product(
        id = "espresso",
        title = "Espresso",
        subtitle = "Strong, short, and bold – the purest coffee shot.",
        imageUrl = unsplash("photo-1510707577719-ae7c14805e3a"),
    ), Product(
        id = "cappuccino",
        title = "Cappuccino",
        subtitle = "Velvety milk foam over a rich espresso base.",
        imageUrl = unsplash("photo-1572442388796-11668a67e53d"),
    ), Product(
        id = "latte",
        title = "Caffè Latte",
        subtitle = "Smooth and milky, perfect for a slow morning.",
        imageUrl = unsplash("photo-1561882468-9110e03e0f78"),
    ), Product(
        id = "mocha",
        title = "Mocha",
        subtitle = "Chocolate meets espresso – a sweet classic.",
        imageUrl = unsplash("photo-1578314675229-c7fb52e9d23c"),
    ), Product(
        id = "iced",
        title = "Iced Coffee",
        subtitle = "Cold, refreshing, and perfect for summer days.",
        imageUrl = unsplash("photo-1517701604599-bb29b565090c"),
    ), Product(
        id = "cookies",
        title = "Fresh Cookies",
        subtitle = "Warm, gooey cookies – perfect with any drink.",
        imageUrl = unsplash("photo-1499636136210-6f4ee915583e"),
    ), Product(
        id = "flat_white",
        title = "Flat White",
        subtitle = "Silky microfoam over a rich double espresso.",
        imageUrl = unsplash("photo-1497935586351-b67a49e012bf"),
    ), Product(
        id = "filter",
        title = "Filter Coffee",
        subtitle = "Slow-brewed, clean and bright in flavor.",
        imageUrl = unsplash("photo-1442550528053-c431ecb55509"),
    ), Product(
        id = "toast",
        title = "Buttered Toast",
        subtitle = "Golden, crispy slices with melted butter.",
        imageUrl = unsplash("photo-1525351484163-7529414344d8"),
    ), Product(
        id = "tea",
        title = "Herbal Tea",
        subtitle = "Calming blend of herbs, no caffeine, all comfort.",
        imageUrl = unsplash("photo-1576092768241-dec231879fc3"),
    ), Product(
        id = "cake",
        title = "Slice of Cake",
        subtitle = "Rich, moist cake – your afternoon treat.",
        imageUrl = unsplash("photo-1565958011703-44f9829ba187"),
    ), Product(
        id = "cupcake",
        title = "Cupcake",
        subtitle = "Small, sweet, and topped with creamy frosting.",
        imageUrl = unsplash("photo-1426869981800-95ebf51ce900"),
    ), Product(
        id = "takeaway",
        title = "Takeaway Coffee",
        subtitle = "Your favorite brew, ready to go.",
        imageUrl = unsplash("photo-1521017432531-fbd92d768814"),
    )
)

/**
 * Build an Unsplash CDN URL for a specific photo ID, with sane resize / quality / format params
 * so the CDN does the heavy lifting of returning a right-sized JPEG/WebP instead of the
 * multi-megapixel original.
 *   • `w=800`             — width in pixels; height scales proportionally
 *   • `q=80`              — JPEG quality (good visual quality, much smaller than 100)
 *   • `auto=format`       — let the CDN serve WebP/AVIF to clients that support it
 *   • `fit=crop`          — center-crop instead of letterboxing
 */
private fun unsplash(photoId: String): String =
    "https://images.unsplash.com/$photoId?w=800&q=80&auto=format&fit=crop"