package com.example.composelearning.animcompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composelearning.ValueBasedAnimationsScreen

sealed class AnimScreen(val route: String) {
    object Home : AnimScreen("home")
    object MathBasics : AnimScreen("math_basics")
    object DrawingFundamentals : AnimScreen("drawing_fundamentals")
    object LinesShapesArcs : AnimScreen("lines_shapes_arcs")
    object PathsComplexShapes : AnimScreen("paths_complex_shapes")
    object ImagesBitmaps : AnimScreen("images_bitmaps")
    object CanvasState : AnimScreen("canvas_state")
    object TouchGestures : AnimScreen("touch_gestures")
    object AnimationBasics : AnimScreen("animation_basics")
    object ValueBasedAnimations : AnimScreen("value_based_animations")
    object TransitionAnimations : AnimScreen("transition_animations")
    object PhysicsAnimations : AnimScreen("physics_animations")
    object GameEnvironment : AnimScreen("game_environment")

    object BottleWaveAnimation : AnimScreen("bottle_wave_animation")

    object DatePickerScreen : AnimScreen("date_picker")

    object FileDeleteAnimation : AnimScreen("file_delete_animation")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AnimScreen.Home.route,
        modifier = modifier
    ) {
        composable(AnimScreen.Home.route) {
            MainHomeScreen(navController)
        }
        composable(AnimScreen.MathBasics.route) {
            MathBasicsScreen()
        }
        composable(AnimScreen.DrawingFundamentals.route) {
            DrawingFundamentalsScreen()
        }
        composable(AnimScreen.LinesShapesArcs.route) {
            LinesShapesArcsScreen()
        }
        composable(AnimScreen.PathsComplexShapes.route) {
            PathsComplexShapesScreen()
        }
        composable(AnimScreen.ImagesBitmaps.route) {
            ImagesBitmapsScreen()
        }
        composable(AnimScreen.CanvasState.route) {
            CanvasStateScreen()
        }
        composable(AnimScreen.TouchGestures.route) {
            TouchGesturesScreen()
        }
        composable(AnimScreen.AnimationBasics.route) {
            NewYearsEveFireworksScreen()
        }
        composable(AnimScreen.ValueBasedAnimations.route) {
            ValueBasedAnimationsScreen()
        }
        composable(AnimScreen.TransitionAnimations.route) {
            TransitionAnimationsScreen()
        }
        composable(AnimScreen.PhysicsAnimations.route) {
            PhysicsAnimationsScreen()
        }
        composable(AnimScreen.GameEnvironment.route) {
            GameEnvironmentScreen()
        }
        composable(AnimScreen.BottleWaveAnimation.route) {
            BottleWaveAnimation()
        }
        composable(AnimScreen.DatePickerScreen.route) {
            PhysicsDatePicker{

            }
        }
        composable(AnimScreen.FileDeleteAnimation.route) {
            FileManagerPreview()
        }
    }
}