package com.example.composelearning.animcompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.composelearning.ValueBasedAnimationsScreen
import com.example.composelearning.anim.AnimatedBalanceDemo
import com.example.composelearning.graphics.BezierCurveSample
import com.example.composelearning.graphics.DrawScaleOnTouch
import com.example.composelearning.graphics.PreviewThermometerCanvas
import kotlinx.serialization.Serializable

@Serializable sealed interface AnimScreen : NavKey {
    @Serializable data object DrawScale: AnimScreen
    @Serializable data object Home : AnimScreen
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
    @Serializable data
    object April2026Features : AnimScreen
    @Serializable data object AnimatedBalance : AnimScreen
    @Serializable data object BezierCurves : AnimScreen
    @Serializable data object SineWave : AnimScreen
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
        entry<AnimScreen.ThermometerAnimation> { PreviewThermometerCanvas() }
        entry<AnimScreen.StackedCards> { TinderSwipeScreen() }
        entry<AnimScreen.April2026Features> { April2026FeaturesScreen(onBack = { navigator.goBack() }) }
        entry<AnimScreen.AnimatedBalance> { AnimatedBalanceDemo() }
        entry<AnimScreen.BezierCurves> { BezierCurveSample(onBack = { navigator.goBack() }) }

        entry<AnimScreen.DrawScale> { DrawScaleOnTouch(onBack = { navigator.goBack() }) }
    }

    NavDisplay(
        modifier = modifier,
        entries = navigationState.toEntries(entryProvider as (NavKey) -> NavEntry<NavKey>),
        onBack = { navigator.goBack() }
    )
}
