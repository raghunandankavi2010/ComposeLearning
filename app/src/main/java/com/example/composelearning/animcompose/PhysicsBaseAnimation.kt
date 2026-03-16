package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

@Composable
fun PhysicsAnimationsScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos = listOf(
        "Spring Physics",
        "Decay Animation",
        "Fling Behavior",
        "Custom Physics",
        "Gesture Physics"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(demos.size) { index ->
                FilterChip(
                    onClick = { selectedDemo = index },
                    label = { Text(demos[index]) },
                    selected = selectedDemo == index
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedDemo) {
                0 -> SpringPhysicsDemo()
                1 -> DecayAnimationDemo()
                2 -> FlingBehaviorDemo()
                else -> Text("Demo not implemented yet", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun SpringPhysicsDemo() {
    data class SpringConfig(val stiffness: Float, val damping: Float)

    var targetPosition by remember { mutableStateOf(Offset(200f, 200f)) }
    var springConfig by remember {
        mutableStateOf(SpringConfig(Spring.StiffnessMedium, Spring.DampingRatioMediumBouncy))
    }

    val animatedPosition = remember { Animatable(Offset(200f, 200f), Offset.VectorConverter) }

    LaunchedEffect(targetPosition, springConfig) {
        animatedPosition.animateTo(
            targetValue = targetPosition,
            animationSpec = spring(
                dampingRatio = springConfig.damping,
                stiffness = springConfig.stiffness
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "Spring Physics",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        // Controls
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Stiffness: ${springConfig.stiffness.toInt()}")
            Slider(
                value = springConfig.stiffness,
                onValueChange = { springConfig = springConfig.copy(stiffness = it) },
                valueRange = 50f..1000f,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Damping: ${String.format("%.2f", springConfig.damping)}")
            Slider(
                value = springConfig.damping,
                onValueChange = { springConfig = springConfig.copy(damping = it) },
                valueRange = 0.1f..2f,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    "Bouncy" to SpringConfig(Spring.StiffnessMedium, Spring.DampingRatioLowBouncy),
                    "Smooth" to SpringConfig(Spring.StiffnessMedium, Spring.DampingRatioNoBouncy),
                    "Quick" to SpringConfig(Spring.StiffnessHigh, Spring.DampingRatioMediumBouncy),
                    "Gentle" to SpringConfig(Spring.StiffnessLow, Spring.DampingRatioMediumBouncy)
                )
                presets.forEach { (name, preset) ->
                    FilterChip(
                        onClick = { springConfig = preset },
                        label = { Text(name) },
                        selected = springConfig == preset
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
                .background(Color.LightGray)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        targetPosition = offset
                    }
                }
        ) {
            // Draw target
            drawCircle(Color.Red.copy(alpha = 0.3f), 30.dp.toPx(), targetPosition)
            drawCircle(Color.Red, 30.dp.toPx(), targetPosition, style = Stroke(width = 2.dp.toPx()))

            // Connection line
            drawLine(
                Color.Gray.copy(alpha = 0.5f),
                animatedPosition.value,
                targetPosition,
                1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )

            // Current position with velocity
            val objectRadius = 20.dp.toPx()
            val velocity = animatedPosition.velocity
            val velocityMagnitude = velocity.getDistance()

            if (velocityMagnitude > 10f) {
                val velocityScale = 0.1f
                val velocityEnd = animatedPosition.value + velocity * velocityScale

                drawLine(Color.Blue, animatedPosition.value, velocityEnd, 3.dp.toPx(), cap = StrokeCap.Round)

                // Arrow head
                val arrowLength = 10.dp.toPx()
                val angle = atan2(velocity.y, velocity.x)
                val arrowAngle1 = angle + PI * 0.8
                val arrowAngle2 = angle - PI * 0.8

                drawLine(Color.Blue, velocityEnd, velocityEnd + Offset(cos(arrowAngle1).toFloat() * arrowLength, sin(arrowAngle1).toFloat() * arrowLength), 3.dp.toPx(), StrokeCap.Round)
                drawLine(Color.Blue, velocityEnd, velocityEnd + Offset(cos(arrowAngle2).toFloat() * arrowLength, sin(arrowAngle2).toFloat() * arrowLength), 3.dp.toPx(), StrokeCap.Round)
            }

            drawCircle(Color.Blue, objectRadius, animatedPosition.value)
            drawCircle(Color.White, objectRadius, animatedPosition.value, style = Stroke(width = 2.dp.toPx()))
        }
    }
}

@Composable
fun DecayAnimationDemo() {
    val density = LocalDensity.current
    var ballPosition by remember { mutableStateOf(Offset(200f, 200f)) }
    var ballVelocity by remember { mutableStateOf(Offset.Zero) }
    var frictionCoefficient by remember { mutableFloatStateOf(0.95f) }
    var isDragging by remember { mutableStateOf(false) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (!isDragging) {
                    val deltaTime = if (lastFrameTime != 0L) {
                        ((frameTimeNanos - lastFrameTime) / 1_000_000f) / 1000f
                    } else {
                        16f / 1000f
                    }
                    lastFrameTime = frameTimeNanos

                    ballVelocity = Offset(
                        ballVelocity.x * frictionCoefficient,
                        ballVelocity.y * frictionCoefficient
                    )

                    val newPosition = ballPosition + ballVelocity * deltaTime
                    val ballRadius = with(density) { 15.dp.toPx() }
                    val bounds = Rect(50f + ballRadius, 50f + ballRadius, 350f - ballRadius, 350f - ballRadius)

                    var newVelocity = ballVelocity

                    if (newPosition.x <= bounds.left || newPosition.x >= bounds.right) {
                        newVelocity = newVelocity.copy(x = -newVelocity.x * 0.8f)
                    }
                    if (newPosition.y <= bounds.top || newPosition.y >= bounds.bottom) {
                        newVelocity = newVelocity.copy(y = -newVelocity.y * 0.8f)
                    }

                    val constrainedPosition = Offset(
                        newPosition.x.coerceIn(bounds.left, bounds.right),
                        newPosition.y.coerceIn(bounds.top, bounds.bottom)
                    )

                    ballPosition = constrainedPosition
                    ballVelocity = newVelocity
                } else {
                    lastFrameTime = frameTimeNanos
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Friction:")
            Slider(
                value = frictionCoefficient,
                onValueChange = { frictionCoefficient = it },
                valueRange = 0.8f..0.99f,
                modifier = Modifier.width(200.dp)
            )
            Text(String.format("%.3f", frictionCoefficient))
        }

        Canvas(
            modifier = Modifier
                .size(400.dp)
                .padding(16.dp)
                .background(Color.White)
                .border(2.dp, Color.Gray)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            ballPosition = offset
                            ballVelocity = Offset.Zero
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            ballPosition = change.position
                            ballVelocity = dragAmount * 60f
                        },
                        onDragEnd = { isDragging = false }
                    )
                }
        ) {
            // Draw bounds
            drawRect(
                Color.LightGray,
                Offset(50.dp.toPx(), 50.dp.toPx()),
                Size(300.dp.toPx(), 300.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Trail
            val trailLength = minOf(20, (ballVelocity.getDistance() / 10f).toInt())
            for (i in 1..trailLength) {
                val alpha = (1f - i.toFloat() / trailLength) * 0.5f
                val trailPosition = ballPosition - ballVelocity * (i * 0.02f)
                val trailSize = 15.dp.toPx() * (1f - i * 0.04f)
                drawCircle(Color.Blue.copy(alpha = alpha), trailSize, trailPosition)
            }

            // Ball
            drawCircle(Color.Blue, 15.dp.toPx(), ballPosition)

            // Velocity vector
            val velocityMagnitude = ballVelocity.getDistance()
            if (velocityMagnitude > 10f) {
                val velocityScale = 0.3f
                drawLine(Color.Red, ballPosition, ballPosition + ballVelocity * velocityScale, 3.dp.toPx(), StrokeCap.Round)
            }
        }
    }
}

data class PhysicsObject(
    val id: Int,
    val position: Offset,
    val velocity: Offset,
    val mass: Float,
    val radius: Float,
    val color: Color,
    val isDragging: Boolean
)

@Composable
fun FlingBehaviorDemo() {
    var objects by remember {
        mutableStateOf(
            List(5) { index ->
                PhysicsObject(
                    id = index,
                    position = Offset(100f + index * 60f, 200f),
                    velocity = Offset.Zero,
                    mass = 1f + index * 0.5f,
                    radius = 20f + index * 5f,
                    color = Color.hsv(index * 72f, 0.8f, 0.9f),
                    isDragging = false
                )
            }
        )
    }
    var draggedObjectId by remember { mutableStateOf<Int?>(null) }
    var isChainMode by remember { mutableStateOf(false) }
    var dragVelocity by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                val deltaTime = 16f / 1000f
                objects = objects.map { obj ->
                    if (!obj.isDragging) {
                        updatePhysicsObject(obj, objects, deltaTime, isChainMode)
                    } else {
                        obj
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isChainMode, onCheckedChange = { isChainMode = it })
            Text("Chain Mode")
            Button(onClick = {
                objects = objects.map { it.copy(
                    velocity = Offset(
                        Random.nextFloat() * 400f - 200f,
                        Random.nextFloat() * 400f - 200f
                    )
                )}
            }) {
                Text("Randomize")
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
                .background(Color.DarkGray)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val touchedObject = objects.find { obj ->
                                (offset - obj.position).getDistance() <= obj.radius
                            }
                            if (touchedObject != null) {
                                draggedObjectId = touchedObject.id
                                objects = objects.map { obj ->
                                    if (obj.id == touchedObject.id) {
                                        obj.copy(isDragging = true, velocity = Offset.Zero)
                                    } else obj
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            draggedObjectId?.let { id ->
                                objects = objects.map { obj ->
                                    if (obj.id == id) {
                                        obj.copy(position = change.position)
                                    } else obj
                                }
                            }
                            dragVelocity = dragAmount * 60f
                        },
                        onDragEnd = {
                            draggedObjectId?.let { id ->
                                objects = objects.map { obj ->
                                    if (obj.id == id) {
                                        obj.copy(isDragging = false, velocity = dragVelocity * 0.5f)
                                    } else obj
                                }
                                draggedObjectId = null
                                dragVelocity = Offset.Zero
                            }
                        }
                    )
                }
        ) {
            // Draw connections in chain mode
            if (isChainMode) {
                for (i in 0 until objects.size - 1) {
                    val obj1 = objects[i]
                    val obj2 = objects[i + 1]
                    val distance = (obj1.position - obj2.position).getDistance()
                    val maxDistance = 100f
                    if (distance <= maxDistance) {
                        val tension = distance / maxDistance
                        val alpha = (1f - tension) * 0.8f
                        val strokeWidth = 2.dp.toPx() + tension * 6.dp.toPx()
                        drawLine(
                            Color.Yellow.copy(alpha = alpha),
                            obj1.position,
                            obj2.position,
                            strokeWidth,
                            StrokeCap.Round
                        )
                    }
                }
            }

            // Draw objects
            objects.forEach { obj ->
                drawPhysicsObject(obj)
            }
        }
    }
}

fun updatePhysicsObject(
    obj: PhysicsObject,
    allObjects: List<PhysicsObject>,
    deltaTime: Float,
    isChainMode: Boolean,
    canvasWidth: Float = 400f,
    canvasHeight: Float = 500f
): PhysicsObject {
    var newVelocity = obj.velocity

    // Gravity
    newVelocity += Offset(0f, 300f * deltaTime)

    // Chain constraints
    if (isChainMode) {
        val neighbors = mutableListOf<PhysicsObject>()
        if (obj.id > 0) allObjects.find { it.id == obj.id - 1 }?.let { neighbors.add(it) }
        if (obj.id < allObjects.size - 1) allObjects.find { it.id == obj.id + 1 }?.let { neighbors.add(it) }

        neighbors.forEach { neighbor ->
            val direction = neighbor.position - obj.position
            val distance = direction.getDistance()
            val restLength = 80f
            if (distance != restLength && distance > 0f) {
                val springForce = (distance - restLength) * 5f
                val force = direction / distance * springForce
                newVelocity += force * deltaTime / obj.mass
            }
        }
    }

    // Collisions
    allObjects.forEach { other ->
        if (other.id != obj.id && !other.isDragging) {
            val direction = obj.position - other.position
            val distance = direction.getDistance()
            val minDistance = obj.radius + other.radius
            if (distance < minDistance && distance > 0f) {
                val overlap = minDistance - distance
                val separationForce = direction / distance * overlap * 10f
                newVelocity += separationForce * deltaTime / obj.mass
            }
        }
    }

    // Damping
    newVelocity *= 0.995f

    // Update position
    val newPosition = obj.position + newVelocity * deltaTime

    // Boundary collision
    val bounds = Rect(obj.radius, obj.radius, canvasWidth - obj.radius, canvasHeight - obj.radius)
    var constrainedPosition = newPosition

    if (newPosition.x <= bounds.left || newPosition.x >= bounds.right) {
        newVelocity = newVelocity.copy(x = -newVelocity.x * 0.7f)
        constrainedPosition = constrainedPosition.copy(x = newPosition.x.coerceIn(bounds.left, bounds.right))
    }
    if (newPosition.y <= bounds.top || newPosition.y >= bounds.bottom) {
        newVelocity = newVelocity.copy(y = -newVelocity.y * 0.7f)
        constrainedPosition = constrainedPosition.copy(y = newPosition.y.coerceIn(bounds.top, bounds.bottom))
    }

    return obj.copy(position = constrainedPosition, velocity = newVelocity)
}

fun DrawScope.drawPhysicsObject(obj: PhysicsObject) {
    val velocityMagnitude = obj.velocity.getDistance()

    // Trail
    if (velocityMagnitude > 20f && !obj.isDragging) {
        val trailLength = minOf(8, (velocityMagnitude / 30f).toInt())
        for (i in 1..trailLength) {
            val alpha = (1f - i.toFloat() / trailLength) * 0.4f
            val trailPos = obj.position - obj.velocity * (i * 0.02f)
            val trailRadius = obj.radius * (1f - i * 0.08f)
            drawCircle(obj.color.copy(alpha = alpha), trailRadius, trailPos)
        }
    }

    // Main object
    drawCircle(
        if (obj.isDragging) obj.color.copy(alpha = 0.8f) else obj.color,
        obj.radius,
        obj.position
    )

    // Border
    val borderColor = if (obj.isDragging) Color.White else Color.Gray
    drawCircle(
        borderColor,
        obj.radius,
        obj.position,
        style = Stroke(width = if (obj.isDragging) 3.dp.toPx() else 1.dp.toPx())
    )

    // Highlight
    drawCircle(
        Color.White.copy(alpha = 0.6f),
        obj.radius * 0.3f,
        obj.position - Offset(obj.radius * 0.3f, obj.radius * 0.3f)
    )
}
