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

package com.example.composelearning

import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.composelearning.anim.AnimatedBalanceDemo
import com.example.composelearning.anim.BiometricDemoPanel
import com.example.composelearning.anim.ButtonAnimationTest
import com.example.composelearning.anim.ValueBasedAnimationsScreen
import com.example.composelearning.anim.ZoomableImageScreen
import com.example.composelearning.animcompose.Navigator
import com.example.composelearning.animcompose.SequentialFadeGrid
import com.example.composelearning.applerings.presentation.ActivityRingsScreen
import com.example.composelearning.breathing.presentation.BreathingScreen
import com.example.composelearning.calendar.CalendarScreen
import com.example.composelearning.calendar.CalendarViewModel
import com.example.composelearning.charts.BarChartShowcaseScreen
import com.example.composelearning.charts.BezierShowcaseScreen
import com.example.composelearning.charts.CandleChartShowcaseScreen
import com.example.composelearning.charts.DonutChartShowcaseScreen
import com.example.composelearning.charts.FitnessLineChartScreen
import com.example.composelearning.charts.PieChartShowcaseScreen
import com.example.composelearning.charts.SpeedometerNavScreen
import com.example.composelearning.charts.TemperatureShowcaseScreen
import com.example.composelearning.cleartodo.presentation.ClearScreen
import com.example.composelearning.clocks.TimeRangeKnobScreen
import com.example.composelearning.customlayout.ArcListSample
import com.example.composelearning.customlayout.CustomPagerSample
import com.example.composelearning.flight.FlightSeatScreen
import com.example.composelearning.foldcard.presentation.FoldCardScreen
import com.example.composelearning.googlecalendar.ui.GoogleCalendarActivity
import com.example.composelearning.gradients.SineWaveMeshGradientScreen
import com.example.composelearning.graphics.AnimatedBorderButton
import com.example.composelearning.graphics.AnimatingWatchDial
import com.example.composelearning.graphics.BlurSample
import com.example.composelearning.graphics.BorderProgressBar
import com.example.composelearning.graphics.DrawScaleOnTouch
import com.example.composelearning.graphics.SineWaveSample
import com.example.composelearning.images.OverlappingImagesScreen
import com.example.composelearning.images.processing.ImageProcessingScreen
import com.example.composelearning.ipodwheel.presentation.IpodScreen
import com.example.composelearning.layouts.PercentageBaseLayout
import com.example.composelearning.lists.AnimatedEntryList
import com.example.composelearning.lists.ListsShowcaseScreen
import com.example.composelearning.pager.ArcCarouselScreen
import com.example.composelearning.pager.PagerShowcaseScreen
import com.example.composelearning.pager.TopRightFanCarouselScreen
import com.example.composelearning.pathmorph.presentation.PathMorphScreen
import com.example.composelearning.peritemvm.PerItemViewModelShowcaseScreen
import com.example.composelearning.permissions.PasskeySample
import com.example.composelearning.progress.SmoothProgressBarScreen
import com.example.composelearning.protobufdemo.ProtobufDemoRoute
import com.example.composelearning.riveo.presentation.RiveoScreen
import com.example.composelearning.shaders.FluidSpringShaderScreen
import com.example.composelearning.shaders.ShadersHubScreen
import com.example.composelearning.shadows.ShadowsShowcaseScreen
import com.example.composelearning.sliders.SquigglySliderSample
import com.example.composelearning.solarsystem.SolarSystemSimulation
import com.example.composelearning.spinningwheel.SpinningWheelRoute
import com.example.composelearning.tabs.TabsSampleNavigation
import com.example.composelearning.textfields.MarqueeText
import com.example.composelearning.textstyling.SquigglySpanSample
import com.example.composelearning.tutorial.ui.TutorialScreen
import com.example.composelearning.wallet.presentation.WalletScreen
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
sealed interface AnimScreen :
    NavKey,
    Parcelable {
    @Serializable data object SequentialFadeGridScreen : AnimScreen

    @Serializable data object AnimatingWatchDial : AnimScreen

    @Serializable data object MeshGradient : AnimScreen

    @Serializable data object DrawScale : AnimScreen

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

    @Serializable data object ArcList : AnimScreen

    @Serializable data object SquigglySpans : AnimScreen

    @Serializable data object Passkeys : AnimScreen

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

    @Serializable data object SpinningWheel : AnimScreen

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

    @Serializable data object AnimatedListEntry : AnimScreen

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

    @Serializable data object ArcCarousel : AnimScreen

    @Serializable data object TabsSample : AnimScreen

    @Serializable data object GoogleCalendar : AnimScreen

    @Serializable data object FluidSpring : AnimScreen

    @Serializable data object RiveoPageCurl : AnimScreen

    @Serializable data object WalletStack : AnimScreen

    @Serializable data object Breathing : AnimScreen

    @Serializable data object PathMorph : AnimScreen

    @Serializable data object ActivityRings : AnimScreen

    @Serializable data object FoldCard : AnimScreen

    @Serializable data object IpodWheel : AnimScreen

    @Serializable data object ClearTodo : AnimScreen

    @Serializable data object GoogleCalling : AnimScreen

    @Serializable data object ProtobufDemo : AnimScreen

    @Serializable data object ParallaxList : AnimScreen

    @Serializable data object UniquePathVisualizer : AnimScreen

    @Serializable data object SolarSystem : AnimScreen

    @Serializable data object GradientHeartFill : AnimScreen

    @Serializable data object WaveLoadingCircle : AnimScreen

    @Serializable data object ShortsFeed : AnimScreen

    @Serializable data object Disintegration : AnimScreen

    /** Second-level home screen listing all demos of one [com.example.composelearning.animcompose.FeatureGroup]. */
    @Serializable @Parcelize
    data class Group(val groupId: String) : AnimScreen
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = AnimScreen.Home,
            topLevelRoutes = setOf(AnimScreen.Home)
        )
    val navigator = remember {
        Navigator(
            navigationState
        )
    }

    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        entry<AnimScreen.Home> {
            com.example.composelearning.animcompose.MainHomeScreen(
                navigator
            )
        }
        entry<AnimScreen.SpinningWheel> { SpinningWheelRoute() }
        entry<AnimScreen.CanvasBasicsHub> { com.example.composelearning.animcompose.CanvasBasicsHubScreen() }
        entry<AnimScreen.MathBasics> { com.example.composelearning.animcompose.MathBasicsScreen() }
        entry<AnimScreen.DrawingFundamentals> { com.example.composelearning.animcompose.DrawingFundamentalsScreen() }
        entry<AnimScreen.LinesShapesArcs> { com.example.composelearning.animcompose.LinesShapesArcsScreen() }
        entry<AnimScreen.PathsComplexShapes> { com.example.composelearning.animcompose.PathsComplexShapesScreen() }
        entry<AnimScreen.ImagesBitmaps> { com.example.composelearning.animcompose.ImagesBitmapsScreen() }
        entry<AnimScreen.CanvasState> { com.example.composelearning.animcompose.CanvasStateScreen() }
        entry<AnimScreen.TouchGestures> { com.example.composelearning.animcompose.TouchGesturesScreen() }
        entry<AnimScreen.AnimationBasics> { com.example.composelearning.animcompose.NewYearsEveFireworksScreen() }
        entry<AnimScreen.ValueBasedAnimations> { ValueBasedAnimationsScreen() }
        entry<AnimScreen.TransitionAnimations> { com.example.composelearning.animcompose.TransitionAnimationsScreen() }
        entry<AnimScreen.PhysicsAnimations> { com.example.composelearning.animcompose.PhysicsAnimationsScreen() }
        entry<AnimScreen.GameEnvironment> { com.example.composelearning.animcompose.GameEnvironmentScreen() }
        entry<AnimScreen.BottleWaveAnimation> { com.example.composelearning.animcompose.BottleWaveAnimation() }
        entry<AnimScreen.DatePickerScreen> { com.example.composelearning.animcompose.PhysicsDatePicker {} }
        entry<AnimScreen.FileDeleteAnimation> { com.example.composelearning.animcompose.FileManagerPreview() }
        entry<AnimScreen.ThermometerAnimation> { com.example.composelearning.animcompose.ThermometerAnimation() }
        entry<AnimScreen.StackedCards> { com.example.composelearning.animcompose.TinderSwipeScreen() }
        entry<AnimScreen.April2026Features> {
            com.example.composelearning.animcompose.April2026FeaturesScreen(onBack = { navigator.goBack() })
        }
        entry<AnimScreen.AnimatedBalance> { AnimatedBalanceDemo() }
        entry<AnimScreen.BezierCurves> { BezierShowcaseScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.SineWave> { SineWaveSample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.CustomPager> { CustomPagerSample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.ArcList> { ArcListSample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.SquigglySpans> { SquigglySpanSample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.Passkeys> { PasskeySample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.SquigglySlider> { SquigglySliderSample(onBack = { navigator.goBack() }) }
        entry<AnimScreen.DrawScale> { DrawScaleOnTouch(onBack = { navigator.goBack() }) }
        entry<AnimScreen.YouTubeStyle> {
            com.example.composelearning.animcompose.YouTubeScreen(onBack = { navigator.goBack() })
        }
        entry<AnimScreen.CircleMenu> { com.example.composelearning.animcompose.CircularMenuScreenWithFullAnimation() }
        entry<AnimScreen.ChatApp> { com.example.composelearning.animcompose.ChatAppNavigation() }
        entry<AnimScreen.SimpleNav> { com.example.composelearning.animcompose.SimpleAppNavigation() }
        entry<AnimScreen.BottomSheet> { com.example.composelearning.animcompose.BottomSheet() }
        entry<AnimScreen.Spotlight> {
            com.example.composelearning.animcompose.SpotlightDemoScreen(onFinish = { navigator.goBack() })
        }
        entry<AnimScreen.TextShimmer> { com.example.composelearning.animcompose.ShimmerTextShowcase() }
        entry<AnimScreen.CardFlip> {
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                com.example.composelearning.animcompose.CreditCardFlip()
            }
        }
        entry<AnimScreen.FluidTabs> { com.example.composelearning.animcompose.FluidTabBarShowcase() }
        entry<AnimScreen.DraggableSheet> { com.example.composelearning.animcompose.DraggableSheetRight() }
        entry<AnimScreen.StaggeredGrid> { com.example.composelearning.animcompose.StaggeredGridDemo() }
        entry<AnimScreen.ParticleHub> { com.example.composelearning.animcompose.ParticleAnimationsHubScreen() }
        entry<AnimScreen.ChartsHub> { com.example.composelearning.animcompose.ChartsHubScreen() }
        entry<AnimScreen.SensorCard> {
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                com.example.composelearning.animcompose.SensorReactiveCard()
            }
        }
        entry<AnimScreen.GradientProgress> { com.example.composelearning.animcompose.MyAwesomeLoadingScreen() }
        entry<AnimScreen.CircularReveal> { com.example.composelearning.animcompose.CircularReveal() }
        entry<AnimScreen.PulsatingCircles> { com.example.composelearning.animcompose.MapsStylePulsatingCircle() }
        entry<AnimScreen.BouncingBall> { com.example.composelearning.animcompose.BouncingBallAnimation() }
        entry<AnimScreen.MultiColorProgress> { com.example.composelearning.animcompose.MultiColorIndeterminateCircularProgressBarPreview() }
        entry<AnimScreen.SharedElementProduct> {
            com.example.composelearning.animcompose.ProductScreen(
                products = com.example.composelearning.animcompose.sampleProducts
            )
        }
        entry<AnimScreen.SidePanelDemo> { com.example.composelearning.animcompose.SidePanelDemoScreen() }
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
                contentAlignment = Alignment.Center
            ) { BiometricDemoPanel() }
        }
        entry<AnimScreen.ButtonAnimation> {
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) { ButtonAnimationTest() }
        }
        entry<AnimScreen.CalendarPicker> {
            CalendarScreen(
                onBackPressed = { navigator.goBack() },
                mainViewModel = viewModel<CalendarViewModel>()
            )
        }
        entry<AnimScreen.BlurEffects> {
            BlurSample(modifier = Modifier.fillMaxSize().systemBarsPadding())
        }
        entry<AnimScreen.ListsShowcase> { ListsShowcaseScreen() }
        entry<AnimScreen.AnimatedListEntry> {
            AnimatedEntryList(
                items = remember { List(30) { "Animated item #${it + 1}" } },
                modifier = Modifier.systemBarsPadding()
            )
        }
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedBorderButton()
                BorderProgressBar()
            }
        }
        entry<AnimScreen.MarqueeDemo> {
            Box(
                modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                MarqueeText(
                    text = "This is a long marquee text that scrolls horizontally when it overflows the available space — Jetpack Compose marquee demo."
                )
            }
        }
        entry<AnimScreen.OverlappingImages> {
            OverlappingImagesScreen(modifier = Modifier.systemBarsPadding())
        }
        entry<AnimScreen.NetflixLogo> { com.example.composelearning.animcompose.NetflixLogoAnimation() }
        entry<AnimScreen.AnmolNetflix> { com.example.composelearning.animcompose.AnmolNetflixIntroAnimation() }
        entry<AnimScreen.SortAnimation> { com.example.composelearning.animcompose.SortAnimationScreen() }
        entry<AnimScreen.ImageProcessing> {
            ImageProcessingScreen(onBack = { navigator.goBack() })
        }
        entry<AnimScreen.SaveActivity> { com.example.composelearning.animcompose.SaveActivityScreen() }
        entry<AnimScreen.ZoomableImage> { ZoomableImageScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.PerItemViewModel> { PerItemViewModelShowcaseScreen() }
        entry<AnimScreen.SmoothProgress> { SmoothProgressBarScreen() }
        entry<AnimScreen.FlightSeat> { FlightSeatScreen() }
        entry<AnimScreen.FanCarousel> { TopRightFanCarouselScreen() }
        entry<AnimScreen.ArcCarousel> { ArcCarouselScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.TabsSample> { TabsSampleNavigation() }
        entry<AnimScreen.GoogleCalendar> {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                context.startActivity(Intent(context, GoogleCalendarActivity::class.java))
            }
            navigator.goBack()
        }
        entry<AnimScreen.FluidSpring> { FluidSpringShaderScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.RiveoPageCurl> { RiveoScreen() }
        entry<AnimScreen.WalletStack> { WalletScreen() }
        entry<AnimScreen.Breathing> { BreathingScreen() }
        entry<AnimScreen.PathMorph> { PathMorphScreen() }
        entry<AnimScreen.ActivityRings> { ActivityRingsScreen() }
        entry<AnimScreen.FoldCard> { FoldCardScreen() }
        entry<AnimScreen.IpodWheel> { IpodScreen() }
        entry<AnimScreen.ClearTodo> { ClearScreen() }
        entry<AnimScreen.GoogleCalling> {
            com.example.composelearning.animcompose.GoogleCallingRoute(onBack = { navigator.goBack() })
        }
        entry<AnimScreen.ProtobufDemo> { ProtobufDemoRoute(onBack = { navigator.goBack() }) }
        entry<AnimScreen.UniquePathVisualizer> { com.example.composelearning.dsa.UniquePathsVisualizer(modifier = Modifier.systemBarsPadding()) }
        entry<AnimScreen.SolarSystem> { SolarSystemSimulation() }
        entry<AnimScreen.GradientHeartFill> { com.example.composelearning.heartfill.GradientHeartFill() }
        entry<AnimScreen.WaveLoadingCircle> { com.example.composelearning.progress.WaveLoadingCircleScreen() }
        entry<AnimScreen.ShortsFeed> { com.example.composelearning.shortsfeed.presentation.ShortsFeedRoute() }
        entry<AnimScreen.Disintegration> { com.example.composelearning.disintegration.DisintegrationScreen() }
        entry<AnimScreen.Group> { key ->
            com.example.composelearning.animcompose.GroupFeaturesScreen(
                groupId = key.groupId,
                navigator = navigator
            )
        }
        entry<AnimScreen.MeshGradient> { SineWaveMeshGradientScreen() }
        entry<AnimScreen.AnimatingWatchDial> { AnimatingWatchDial() }
        entry<AnimScreen.SequentialFadeGridScreen> { SequentialFadeGrid() }
    }

    NavDisplay(
        modifier = modifier,
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}
