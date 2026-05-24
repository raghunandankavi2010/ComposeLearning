/*
 * Copyright 2024 Raghunandan Kavi
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

package com.example.composelearning.animcompose

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.ValueBasedAnimationsScreen
import com.example.composelearning.anim.AnimatedBalanceDemo
import com.example.composelearning.anim.ZoomableImageScreen
import com.example.composelearning.PercentageBaseLayout
import com.example.composelearning.anim.BiometricDemoPanel
import com.example.composelearning.anim.ButtonAnimationTest
import com.example.composelearning.calendar.CalendarScreen
import com.example.composelearning.calendar.CalendarViewModel
import com.example.composelearning.graphics.AnimatedBorderButton
import com.example.composelearning.graphics.BlurSample
import com.example.composelearning.graphics.BorderProgressBar
import com.example.composelearning.images.OverlappingImagesScreen
import com.example.composelearning.images.processing.ImageProcessingScreen
import com.example.composelearning.lists.ListsShowcaseScreen
import com.example.composelearning.pager.PagerShowcaseScreen
import com.example.composelearning.pager.TopRightFanCarouselScreen
import com.example.composelearning.tabs.TabsSampleNavigation
import com.example.composelearning.peritemvm.PerItemViewModelShowcaseScreen
import com.example.composelearning.progess.SmoothProgressBarScreen
import com.example.composelearning.flight.FlightSeatScreen
import com.example.composelearning.textfields.MarqueeText
import com.example.composelearning.charts.*
import com.example.composelearning.customlayout.CustomPagerSample
import com.example.composelearning.graphics.DrawScaleOnTouch
import com.example.composelearning.graphics.SineWaveSample
import com.example.composelearning.textstyling.SquigglySpanSample
import com.example.composelearning.sliders.SquigglySliderSample
import com.example.composelearning.shaders.ShadersHubScreen
import com.example.composelearning.clocks.TimeRangeKnobScreen
import com.example.composelearning.shadows.ShadowsShowcaseScreen
import com.example.composelearning.tutorial.ui.TutorialScreen
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
sealed interface AnimScreen : NavKey, Parcelable {
    @Serializable data object DrawScale: AnimScreen
    @Serializable data object Home : AnimScreen
    @Serializable data object CanvasBasicsHub : AnimScreen
    @Serializable data object MathBasics : AnimScreen
    @Serializable data object DrawingFundamentals : AnimScreen
    @Serializable data object LinesShapesArcs : AnimScreen
    @Serializable data object PathsComplexShapes : AnimScreen
    @Serializable data object ImagesBitmaps : AnimScreen
    @Serializable data object CanvasState : AnimScreen
    @Serializable data object TouchGestures : AnimScreen
    @Serializable data object AnimationBasics : AnimScreen
    @Serializable data object ValueBasedAnimations : AnimScreen
    @Serializable data object TransitionAnimations : AnimScreen
    @Serializable data object PhysicsAnimations : AnimScreen
    @Serializable data object GameEnvironment : AnimScreen
    @Serializable data object BottleWaveAnimation : AnimScreen
    @Serializable data object DatePickerScreen : AnimScreen
    @Serializable data object FileDeleteAnimation : AnimScreen
    @Serializable data object ThermometerAnimation : AnimScreen
    @Serializable data object StackedCards : AnimScreen
    @Serializable data object April2026Features : AnimScreen
    @Serializable data object AnimatedBalance : AnimScreen
    @Serializable data object BezierCurves : AnimScreen
    @Serializable data object SineWave : AnimScreen
    @Serializable data object CustomPager : AnimScreen
    @Serializable data object SquigglySpans : AnimScreen
    @Serializable data object SquigglySlider : AnimScreen
    @Serializable data object InboxRecyclerView : AnimScreen
    @Serializable data object YouTubeStyle : AnimScreen
    @Serializable data object CircleMenu : AnimScreen
    @Serializable data object ChatApp : AnimScreen
    @Serializable data object SimpleNav : AnimScreen
    @Serializable data object BottomSheet : AnimScreen
    @Serializable data object Spotlight : AnimScreen
    @Serializable data object TextShimmer : AnimScreen
    @Serializable data object CardFlip : AnimScreen
    @Serializable data object FluidTabs : AnimScreen
    @Serializable data object DraggableSheet : AnimScreen
    @Serializable data object StaggeredGrid : AnimScreen
    @Serializable data object ParticleHub : AnimScreen
    @Serializable data object ChartsHub : AnimScreen
    @Serializable data object SensorCard : AnimScreen
    @Serializable data object GradientProgress : AnimScreen
    @Serializable data object CircularReveal : AnimScreen
    @Serializable data object PulsatingCircles : AnimScreen
    @Serializable data object BouncingBall : AnimScreen
    @Serializable data object MultiColorProgress : AnimScreen
    @Serializable data object SharedElementProduct : AnimScreen
    @Serializable data object SidePanelDemo : AnimScreen
    @Serializable data object Speedometer : AnimScreen
    @Serializable data object FitnessLineChart : AnimScreen
    @Serializable data object BarChartDemo : AnimScreen
    @Serializable data object DonutChartDemo : AnimScreen
    @Serializable data object PieChartDemo : AnimScreen
    @Serializable data object CandleChartDemo : AnimScreen
    @Serializable data object TemperatureGaugeDemo : AnimScreen
    @Serializable data object ShaderDemos : AnimScreen
    @Serializable data object TutorialOverlay : AnimScreen
    @Serializable data object ShadowsPlayground : AnimScreen
    @Serializable data object TimeRangeKnob : AnimScreen
    @Serializable data object BiometricDemo : AnimScreen
    @Serializable data object ButtonAnimation : AnimScreen
    @Serializable data object CalendarPicker : AnimScreen
    @Serializable data object BlurEffects : AnimScreen
    @Serializable data object ListsShowcase : AnimScreen
    @Serializable data object PercentageLayout : AnimScreen
    @Serializable data object PathProgress : AnimScreen
    @Serializable data object PagerShowcase : AnimScreen
    @Serializable data object MarqueeDemo : AnimScreen
    @Serializable data object OverlappingImages : AnimScreen
    @Serializable data object NetflixLogo : AnimScreen
    @Serializable data object AnmolNetflix : AnimScreen
    @Serializable data object SortAnimation : AnimScreen
    @Serializable data object ImageProcessing : AnimScreen
    @Serializable data object SaveActivity : AnimScreen
    @Serializable data object ZoomableImage : AnimScreen
    @Serializable data object PerItemViewModel : AnimScreen
    @Serializable data object SmoothProgress : AnimScreen
    @Serializable data object FlightSeat : AnimScreen
    @Serializable data object FanCarousel : AnimScreen
    @Serializable data object TabsSample : AnimScreen
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navigationState = rememberNavigationState(
        startRoute = AnimScreen.Home,
        topLevelRoutes = setOf(AnimScreen.Home)
    )
    val navigator = remember { Navigator(navigationState) }

    val entryProvider: (NavKey) -> NavEntry<out NavKey> = entryProvider {
        entry<AnimScreen.Home> { MainHomeScreen(navigator) }
        entry<AnimScreen.CanvasBasicsHub> { CanvasBasicsHubScreen() }
        entry<AnimScreen.MathBasics> { MathBasicsScreen() }
        entry<AnimScreen.DrawingFundamentals> { DrawingFundamentalsScreen() }
        entry<AnimScreen.LinesShapesArcs> { LinesShapesArcsScreen() }
        entry<AnimScreen.PathsComplexShapes> { PathsComplexShapesScreen() }
        entry<AnimScreen.ImagesBitmaps> { ImagesBitmapsScreen() }
        entry<AnimScreen.CanvasState> { CanvasStateScreen() }
        entry<AnimScreen.TouchGestures> { TouchGesturesScreen() }
        entry<AnimScreen.AnimationBasics> { NewYearsEveFireworksScreen() }
        entry<AnimScreen.ValueBasedAnimations> { ValueBasedAnimationsScreen() }
        entry<AnimScreen.TransitionAnimations> { TransitionAnimationsScreen() }
        entry<AnimScreen.PhysicsAnimations> { PhysicsAnimationsScreen() }
        entry<AnimScreen.GameEnvironment> { GameEnvironmentScreen() }
        entry<AnimScreen.BottleWaveAnimation> { BottleWaveAnimation() }
        entry<AnimScreen.DatePickerScreen> { PhysicsDatePicker {} }
        entry<AnimScreen.FileDeleteAnimation> { FileManagerPreview() }
        entry<AnimScreen.ThermometerAnimation> { ThermometerAnimation() }
        entry<AnimScreen.StackedCards> { TinderSwipeScreen() }
        entry<AnimScreen.April2026Features> { April2026FeaturesScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.AnimatedBalance> { AnimatedBalanceDemo() }
        entry<AnimScreen.BezierCurves> { BezierShowcaseScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.SineWave> { SineWaveSample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.CustomPager> { CustomPagerSample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.SquigglySpans> { SquigglySpanSample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.DrawScale> { DrawScaleOnTouch(onBack = { navigator.goBack() }) }
        entry<AnimScreen.YouTubeStyle> { YouTubeScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.CircleMenu> { CircularMenuScreenWithFullAnimation() }
        entry<AnimScreen.ChatApp> { ChatAppNavigation() }
        entry<AnimScreen.SimpleNav> { SimpleAppNavigation() }
        entry<AnimScreen.BottomSheet> { BottomSheet() }
        entry<AnimScreen.Spotlight> { SpotlightDemoScreen(onFinish = { navigator.goBack() }) }
        entry<AnimScreen.TextShimmer> { ShimmerTextShowcase() }
        entry<AnimScreen.CardFlip> { 
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                CreditCardFlip() 
            }
        }
        entry<AnimScreen.FluidTabs> { FluidTabBarShowcase() }
        entry<AnimScreen.DraggableSheet> { DraggableSheetRight() }
        entry<AnimScreen.StaggeredGrid> { StaggeredGridDemo() }
        entry<AnimScreen.ParticleHub> { ParticleAnimationsHubScreen() }
        entry<AnimScreen.ChartsHub> { ChartsHubScreen() }
        entry<AnimScreen.SensorCard> { 
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                SensorReactiveCard() 
            }
        }
        entry<AnimScreen.GradientProgress> { MyAwesomeLoadingScreen() }
        entry<AnimScreen.CircularReveal> { CircularReveal() }
        entry<AnimScreen.PulsatingCircles> { MapsStylePulsatingCircle() }
        entry<AnimScreen.BouncingBall> { BouncingBallAnimation() }
        entry<AnimScreen.MultiColorProgress> { MultiColorIndeterminateCircularProgressBarPreview() }
        entry<AnimScreen.SharedElementProduct> { ProductScreen(products = sampleProducts) }
        entry<AnimScreen.SidePanelDemo> { SidePanelDemoScreen() }
        entry<AnimScreen.Speedometer> { SpeedometerNavScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.FitnessLineChart> { FitnessLineChartScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.BarChartDemo> { BarChartShowcaseScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.DonutChartDemo> { DonutChartShowcaseScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.PieChartDemo> { PieChartShowcaseScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.CandleChartDemo> { CandleChartShowcaseScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.TemperatureGaugeDemo> { TemperatureShowcaseScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.ShaderDemos> { ShadersHubScreen() }
        entry<AnimScreen.TutorialOverlay> { TutorialScreen() }
        entry<AnimScreen.ShadowsPlayground> { ShadowsShowcaseScreen() }
        entry<AnimScreen.TimeRangeKnob> { TimeRangeKnobScreen() }
        entry<AnimScreen.BiometricDemo> {
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                contentAlignment = Alignment.Center,
            ) { BiometricDemoPanel() }
        }
        entry<AnimScreen.ButtonAnimation> {
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                contentAlignment = Alignment.Center,
            ) { ButtonAnimationTest() }
        }
        entry<AnimScreen.CalendarPicker> {
            CalendarScreen(
                onBackPressed = { navigator.goBack() },
                mainViewModel = viewModel<CalendarViewModel>(),
            )
        }
        entry<AnimScreen.BlurEffects> {
            BlurSample(modifier = Modifier.fillMaxSize().systemBarsPadding())
        }
        entry<AnimScreen.ListsShowcase> { ListsShowcaseScreen() }
        entry<AnimScreen.PagerShowcase> { PagerShowcaseScreen() }
        entry<AnimScreen.PercentageLayout> {
            Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) { PercentageBaseLayout() }
        }
        entry<AnimScreen.PathProgress> {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedBorderButton()
                BorderProgressBar()
            }
        }
        entry<AnimScreen.MarqueeDemo> {
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                MarqueeText(
                    text = "This is a long marquee text that scrolls horizontally when it overflows the available space — Jetpack Compose marquee demo.",
                )
            }
        }
        entry<AnimScreen.OverlappingImages> {
            OverlappingImagesScreen(modifier = Modifier.systemBarsPadding())
        }
        entry<AnimScreen.NetflixLogo> { NetflixLogoAnimation() }
        entry<AnimScreen.AnmolNetflix> { AnmolNetflixIntroAnimation() }
        entry<AnimScreen.SortAnimation> { SortAnimationScreen() }
        entry<AnimScreen.ImageProcessing> {
            ImageProcessingScreen(onBack = { navigator.goBack() })
        }
        entry<AnimScreen.SaveActivity> { SaveActivityScreen() }
        entry<AnimScreen.ZoomableImage> { ZoomableImageScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.PerItemViewModel> { PerItemViewModelShowcaseScreen() }
        entry<AnimScreen.SmoothProgress> { SmoothProgressBarScreen() }
        entry<AnimScreen.FlightSeat> { FlightSeatScreen() }
        entry<AnimScreen.FanCarousel> { TopRightFanCarouselScreen() }
        entry<AnimScreen.TabsSample> { TabsSampleNavigation() }
    }

    NavDisplay(
        modifier = modifier,
        entries = navigationState.toEntries(entryProvider as (NavKey) -> NavEntry<NavKey>),
        onBack = { navigator.goBack() }
    )
}
