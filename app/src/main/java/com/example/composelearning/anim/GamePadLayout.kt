package com.example.composelearning.anim

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
// import androidx.compose.foundation.focusable // Focusable might not be needed for all joystick elements
import androidx.compose.foundation.gestures.detectDragGestures
// import androidx.compose.foundation.interaction.MutableInteractionSource // Not directly used in this version
// import androidx.compose.foundation.interaction.collectIsPressedAsState // Not directly used in this version
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
// import androidx.constraintlayout.compose.ConstraintSet // Not used in this direct layout approach
import androidx.constraintlayout.compose.Dimension
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.composelearning.R // IMPORTANT: Ensure this R file is correct and drawables exist
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
// import kotlin.math.min // Not used in this version
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// Define your colors
val GamepadColor = Color(0xFF333333)
val ButtonTextColor = Color(0xFFBFBFFD)
val YButtonColor = Color(0xFF74E2FF)
val XButtonColor = Color(0xFF0BBB81)
val BButtonColor = Color(0xFFDC3E72)
val AButtonColor = Color(0xFFB15EFE)
val ActionButtonContainerColor = Color.Transparent //Color(0x40FFFFFF) // Semi-transparent for example
val DPadContainerColor = Color.Transparent //Color(0x40FFFFFF)
val DefaultButtonTextColor = Color.White // For text on colored buttons

// Placeholder for your actual drawable resources - REPLACE THESE
object PlaceholderDrawables {
    // YOU MUST REPLACE THESE WITH YOUR ACTUAL DRAWABLE RESOURCES (e.g., R.drawable.my_analog_bg)
    val ic_analog_bg = R.drawable.ic_launcher_background // REPLACE
    val ic_analog = R.drawable.ic_launcher_foreground    // REPLACE
    val ic_controller = android.R.drawable.ic_menu_send // REPLACE
    // Add other selectors if they are specific images and not just color changes
    // For simplicity, ButtonDefaults.buttonColors handles basic color states.
    // If your selectors are complex drawables, you'll need ImageButtons or custom drawing.
}

fun Modifier.keepScreenOn(): Modifier = composed {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    this
}

@Composable
fun GamepadLayout() {
    var leftStickVisible by remember { mutableStateOf(false) }
    var rightStickVisible by remember { mutableStateOf(false) }
    var leftStickOffset by remember { mutableStateOf(Offset.Zero) }
    var rightStickOffset by remember { mutableStateOf(Offset.Zero) }

    val density = LocalDensity.current

    val maxStickTravelRadius = 40.dp // Example, adjust based on your joystick area size

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(GamepadColor)
            .padding(horizontal = 15.dp, vertical = 10.dp)
            .keepScreenOn()
    ) {
        // Create refs individually
        val leftTriggerGroupRef = createRef()
        val rightTriggerGroupRef = createRef()

        val actionButtonsBoxRef = createRef()
        val dpadBoxRef = createRef()

        val leftJoyStickAreaRef = createRef()
        val stickLeftBgRef = createRef()
        val stickLeftRef = createRef()

        val rightJoyStickAreaRef = createRef()
        val stickRightBgRef = createRef()
        val stickRightRef = createRef()

        val backButtonRef = createRef()
        val homeButtonRef = createRef()
        val startButtonRef = createRef()
        val controllerButtonRef = createRef()
        val toggleButtonRef = createRef()


        // Guidelines
        val h25 = createGuidelineFromTop(0.06f) // approx 30dp / typical screen height
        val h1 = createGuidelineFromTop(0.52f)
        val v25 = createGuidelineFromStart(0.25f)
        val v50 = createGuidelineFromStart(0.5f)
        val v75 = createGuidelineFromStart(0.75f)
        val vb1 = createGuidelineFromStart(0.4167f)
        val vb2 = createGuidelineFromStart(0.5833f)


        // --- Left Triggers ---
        Column(
            modifier = Modifier.constrainAs(leftTriggerGroupRef) {
                start.linkTo(parent.start)
                top.linkTo(h25, margin = 8.dp)
            }
        ) {
            Row {
                Button(onClick = { /* L2 */ }, modifier = Modifier.width(60.dp).height(40.dp).padding(start = 10.dp, top = 10.dp, bottom = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("L2", color = ButtonTextColor, fontSize = 12.sp) }
                Button(onClick = { /* L1 */ }, modifier = Modifier.width(60.dp).height(40.dp).padding(end = 10.dp, top = 10.dp, bottom = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("L1", color = ButtonTextColor, fontSize = 12.sp) }
            }
            Button(onClick = { /* L3 */ }, modifier = Modifier.width(60.dp).height(40.dp).padding(horizontal = 10.dp, vertical = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("L3", color = ButtonTextColor, fontSize = 12.sp) }
        }

        // --- Right Triggers ---
        Column(
            modifier = Modifier.constrainAs(rightTriggerGroupRef) {
                end.linkTo(parent.end)
                top.linkTo(h25, margin = 8.dp)
            },
            horizontalAlignment = Alignment.End
        ) {
            Row {
                Button(onClick = { /* R1 */ }, modifier = Modifier.width(60.dp).height(40.dp).padding(start = 10.dp, top = 10.dp, bottom = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("R1", color = ButtonTextColor, fontSize = 12.sp) }
                Button(onClick = { /* R2 */ }, modifier = Modifier.width(60.dp).height(40.dp).padding(end = 10.dp, top = 10.dp, bottom = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("R2", color = ButtonTextColor, fontSize = 12.sp) }
            }
            Button(onClick = { /* R3 */ }, modifier = Modifier.width(60.dp).height(40.dp).padding(horizontal = 10.dp, vertical = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("R3", color = ButtonTextColor, fontSize = 12.sp) }
        }

        // --- Action Buttons (Y, X, B, A) ---
        Box(
            modifier = Modifier
                .constrainAs(actionButtonsBoxRef) {
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(v75)
                    top.linkTo(h1)
                }
                .aspectRatio(1f)
                .background(ActionButtonContainerColor)
        ) {
            Button(onClick = { /* Y */ }, modifier = Modifier.align(Alignment.TopCenter).size(40.dp), colors = ButtonDefaults.buttonColors(containerColor = YButtonColor)) { Text("Y", color = DefaultButtonTextColor, fontSize = 12.sp) }
            Button(onClick = { /* X */ }, modifier = Modifier.align(Alignment.CenterStart).size(40.dp), colors = ButtonDefaults.buttonColors(containerColor = XButtonColor)) { Text("X", color = DefaultButtonTextColor, fontSize = 12.sp) }
            Button(onClick = { /* B */ }, modifier = Modifier.align(Alignment.CenterEnd).size(40.dp), colors = ButtonDefaults.buttonColors(containerColor = BButtonColor)) { Text("B", color = DefaultButtonTextColor, fontSize = 12.sp) }
            Button(onClick = { /* A */ }, modifier = Modifier.align(Alignment.BottomCenter).size(40.dp), colors = ButtonDefaults.buttonColors(containerColor = AButtonColor)) { Text("A", color = DefaultButtonTextColor, fontSize = 12.sp) }
        }

        // --- D-Pad ---
        Box(
            modifier = Modifier
                .constrainAs(dpadBoxRef) {
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                    bottom.linkTo(parent.bottom)
                    end.linkTo(v25)
                    start.linkTo(parent.start)
                    top.linkTo(h1)
                }
                .aspectRatio(1f)
                .background(DPadContainerColor)
        ) {
            Button(onClick = { /* Up */ }, modifier = Modifier.align(Alignment.TopCenter).size(width = 30.dp, height = 40.dp), colors = ButtonDefaults.buttonColors(Color.LightGray)) { /* Icon or empty */ }
            Button(onClick = { /* Left */ }, modifier = Modifier.align(Alignment.CenterStart).size(width = 40.dp, height = 30.dp), colors = ButtonDefaults.buttonColors(Color.LightGray)) { /* Icon or empty */ }
            Button(onClick = { /* Right */ }, modifier = Modifier.align(Alignment.CenterEnd).size(width = 40.dp, height = 30.dp), colors = ButtonDefaults.buttonColors(Color.LightGray)) { /* Icon or empty */ }
            Button(onClick = { /* Down */ }, modifier = Modifier.align(Alignment.BottomCenter).size(width = 30.dp, height = 40.dp), colors = ButtonDefaults.buttonColors(Color.LightGray)) { /* Icon or empty */ }
        }

        // --- Left Joystick Visual Background ---
        Image(
            painter = painterResource(id = PlaceholderDrawables.ic_analog_bg),
            contentDescription = "Left Analog Stick Background",
            modifier = Modifier
                .constrainAs(stickLeftBgRef) {
                    start.linkTo(v25)
                    end.linkTo(v50)
                    top.linkTo(h1)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .aspectRatio(1f)
                .alpha(if (leftStickVisible) 1f else 0f)
        )

        // --- Left Joystick Visual Stick (Knob) ---
        Image(
            painter = painterResource(id = PlaceholderDrawables.ic_analog),
            contentDescription = "Left Analog Stick",
            modifier = Modifier
                .constrainAs(stickLeftRef) {
                    centerTo(stickLeftBgRef)
                    width = Dimension.percent(0.4f)
                    height = Dimension.ratio("1:1")
                }
                .offset { IntOffset(leftStickOffset.x.roundToInt(), leftStickOffset.y.roundToInt()) }
                .alpha(if (leftStickVisible) 1f else 0f)
        )

        // --- Left Joystick Touch Area ---
        Box(
            modifier = Modifier
                .constrainAs(leftJoyStickAreaRef) {
                    top.linkTo(stickLeftBgRef.top)
                    bottom.linkTo(stickLeftBgRef.bottom)
                    start.linkTo(stickLeftBgRef.start)
                    end.linkTo(stickLeftBgRef.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> leftStickVisible = true; /* Store initial offset if needed */ },
                        onDragEnd = {
                            leftStickVisible = false
                            leftStickOffset = Offset.Zero
                            // TODO: Send neutral joystick state (0,0) to gamepad logic
                            println("Left Stick Released: Neutral")
                        },
                        onDragCancel = {
                            leftStickVisible = false
                            leftStickOffset = Offset.Zero
                            // TODO: Send neutral joystick state (0,0) to gamepad logic
                            println("Left Stick Cancelled: Neutral")
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentOffset = leftStickOffset + dragAmount
                            val distance = sqrt(currentOffset.x.pow(2) + currentOffset.y.pow(2))
                            val maxRadiusPx = with(density) { maxStickTravelRadius.toPx() }

                            if (distance <= maxRadiusPx) {
                                leftStickOffset = currentOffset
                            } else {
                                val angle = atan2(currentOffset.y, currentOffset.x)
                                leftStickOffset = Offset(maxRadiusPx * cos(angle), maxRadiusPx * sin(angle))
                            }
                            // TODO: Convert leftStickOffset to normalized values (-1 to 1) and send
                            // val normalizedX = leftStickOffset.x / maxRadiusPx
                            // val normalizedY = leftStickOffset.y / maxRadiusPx
                            // println("Left Stick Drag: ($normalizedX, $normalizedY)")
                        }
                    )
                }
        )

        // --- Right Joystick (Visual Background) ---
        Image(
            painter = painterResource(id = PlaceholderDrawables.ic_analog_bg),
            contentDescription = "Right Analog Stick Background",
            modifier = Modifier
                .constrainAs(stickRightBgRef) {
                    start.linkTo(v50)
                    end.linkTo(v75)
                    top.linkTo(h1)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .aspectRatio(1f)
                .alpha(if (rightStickVisible) 1f else 0f)
        )
        // --- Right Joystick (Visual Stick) ---
        Image(
            painter = painterResource(id = PlaceholderDrawables.ic_analog),
            contentDescription = "Right Analog Stick",
            modifier = Modifier
                .constrainAs(stickRightRef) {
                    centerTo(stickRightBgRef)
                    width = Dimension.percent(0.4f)
                    height = Dimension.ratio("1:1")
                }
                .offset { IntOffset(rightStickOffset.x.roundToInt(), rightStickOffset.y.roundToInt()) }
                .alpha(if (rightStickVisible) 1f else 0f)
        )
        // --- Right Joystick (Touch Area) ---
        Box(
            modifier = Modifier
                .constrainAs(rightJoyStickAreaRef) {
                    top.linkTo(stickRightBgRef.top)
                    bottom.linkTo(stickRightBgRef.bottom)
                    start.linkTo(stickRightBgRef.start)
                    end.linkTo(stickRightBgRef.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { _ -> rightStickVisible = true },
                        onDragEnd = {
                            rightStickVisible = false
                            rightStickOffset = Offset.Zero
                            // TODO: Send neutral right joystick state
                            println("Right Stick Released: Neutral")
                        },
                        onDragCancel = {
                            rightStickVisible = false
                            rightStickOffset = Offset.Zero
                            // TODO: Send neutral right joystick state
                            println("Right Stick Cancelled: Neutral")
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentOffset = rightStickOffset + dragAmount
                            val distance = sqrt(currentOffset.x.pow(2) + currentOffset.y.pow(2))
                            val maxRadiusPx = with(density) { maxStickTravelRadius.toPx() }
                            if (distance <= maxRadiusPx) {
                                rightStickOffset = currentOffset
                            } else {
                                val angle = atan2(currentOffset.y, currentOffset.x)
                                rightStickOffset = Offset(maxRadiusPx * cos(angle), maxRadiusPx * sin(angle))
                            }
                            // TODO: Convert and send right stick normalized values
                        }
                    )
                }
        )

        // --- Control Buttons (Back, Home, Start) ---
        Button(
            onClick = { /* Back */ }, modifier = Modifier.constrainAs(backButtonRef) {
                width = Dimension.fillToConstraints; height = Dimension.value(35.dp)
                start.linkTo(v25, margin = 10.dp); end.linkTo(vb1); top.linkTo(h25)
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) { Text("Back", color = ButtonTextColor, fontSize = 12.sp) }

        Button(
            onClick = { /* Home/Select */ }, modifier = Modifier.constrainAs(homeButtonRef) {
                width = Dimension.fillToConstraints; height = Dimension.value(35.dp)
                start.linkTo(vb1); end.linkTo(vb2); top.linkTo(h25)
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) { Text("Select", color = ButtonTextColor, fontSize = 12.sp) }

        Button(
            onClick = { /* Start */ }, modifier = Modifier.constrainAs(startButtonRef) {
                width = Dimension.fillToConstraints; height = Dimension.value(35.dp)
                start.linkTo(vb2); end.linkTo(v75) // Adjusted to avoid negative margin for compose
                top.linkTo(h25)
            }.padding(start = 10.dp, end = 20.dp), // Use padding to achieve visual offset of original translationX="-10dp"
            // The XML had end margin 10dp AND translationX -10dp.
            // If it means start is at vb2 and end is at v75 - 10dp(margin) - 10dp(translation):
            // Then constrain end.linkTo(v75) and use .offset(x = (-10).dp)
            // Or adjust end margin: end.linkTo(v75, margin = (-10).dp) if allowed, or more complex guideline.
            // For now, using padding to give it a bit of visual shift.
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) { Text("Start", color = ButtonTextColor, fontSize = 12.sp) }

        // --- Controller Image Button ---
        Image(
            painter = painterResource(id = PlaceholderDrawables.ic_controller),
            contentDescription = "Controller Icon",
            modifier = Modifier
                .constrainAs(controllerButtonRef) {
                    start.linkTo(vb1); end.linkTo(vb2)
                    top.linkTo(parent.top, margin = 10.dp)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }
                .clickable { /* Controller icon click */ }
        )

        // --- Toggle Button (conditionally rendered for visibility 'gone') ---
        val isToggleVisible = false // Control this state as needed
        if (isToggleVisible) {
            Button(
                onClick = { /* Toggle */ },
                modifier = Modifier.constrainAs(toggleButtonRef) {
                    // Example: top right with bias from XML if that was the intent
                    end.linkTo(parent.end, margin = 10.dp) // Based on XML's layout_margin="10dp"
                    top.linkTo(parent.top, margin = 10.dp)
                    // If XML's bias was significant, it would be:
                    // start.linkTo(parent.start)
                    // end.linkTo(parent.end)
                    // horizontalBias = 0.8f // app:layout_constraintHorizontal_bias="0.8"
                    // top.linkTo(parent.top)
                    // verticalBias = 0.0f // app:layout_constraintVertical_bias="0.0" (top aligned)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) { Text("Toggle", color = ButtonTextColor, fontSize = 12.sp) }
        }
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 400) // Adjusted from original
@Composable
fun PreviewGamepadLayout() {
    GamepadLayout()
}
