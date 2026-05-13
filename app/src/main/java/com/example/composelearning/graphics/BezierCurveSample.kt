package com.example.composelearning.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BezierCurveSample(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bezier Curves Deep Dive") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        BezierCurveSampleContent(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        )
    }
}

/**
 * Body of [BezierCurveSample] extracted so the same content can be embedded inside a tab without
 * nesting another Scaffold/TopAppBar.
 */
@Composable
fun BezierCurveSampleContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Understanding Bezier Curves",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        MathDetailsSection()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Interactive Playground",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Drag points to explore. Scrolling is enabled when not dragging a point.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        InteractiveBezierDemo()

        Spacer(modifier = Modifier.height(32.dp))

        FigmaDeepDiveSection()

        Spacer(modifier = Modifier.height(32.dp))

        ComplexFigmaExample()

        Spacer(modifier = Modifier.height(32.dp))

        FigmaToComposeSection()
    }
}

@Composable
fun MathDetailsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "The Math Behind It",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Bezier curves interpolate between points. 't' is the interpolation factor (0 to 1).",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "Quadratic (3 points):", fontWeight = FontWeight.SemiBold)
            Text(
                text = "B(t) = (1-t)²P₀ + 2(1-t)tP₁ + t²P₂",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Cubic (4 points):", fontWeight = FontWeight.SemiBold)
            Text(
                text = "B(t) = (1-t)³P₀ + 3(1-t)²tP₁ + 3(1-t)t²P₂ + t³P₃",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun InteractiveBezierDemo() {
    var isCubic by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    
    var p0Norm by remember { mutableStateOf(Offset(0.1f, 0.8f)) }
    var p1Norm by remember { mutableStateOf(Offset(0.3f, 0.2f)) }
    var p2Norm by remember { mutableStateOf(Offset(0.7f, 0.2f)) }
    var p3Norm by remember { mutableStateOf(Offset(0.9f, 0.8f)) }

    val textMeasurer = rememberTextMeasurer()

    Column {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Quadratic")
            Switch(checked = isCubic, onCheckedChange = { isCubic = it })
            Text("Cubic")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.White, shape = MaterialTheme.shapes.medium)
                .pointerInput(isCubic, canvasSize) {
                    if (canvasSize == Size.Zero) return@pointerInput
                    
                    detectDragGestures { change, dragAmount ->
                        val pos = change.position
                        val threshold = 60f
                        
                        val p0 = Offset(p0Norm.x * canvasSize.width, p0Norm.y * canvasSize.height)
                        val p1 = Offset(p1Norm.x * canvasSize.width, p1Norm.y * canvasSize.height)
                        val p2 = Offset(p2Norm.x * canvasSize.width, p2Norm.y * canvasSize.height)
                        val p3 = Offset(p3Norm.x * canvasSize.width, p3Norm.y * canvasSize.height)

                        fun updatePoint(point: Offset, delta: Offset): Offset {
                            val newX = (point.x + delta.x).coerceIn(0f, canvasSize.width)
                            val newY = (point.y + delta.y).coerceIn(0f, canvasSize.height)
                            return Offset(newX / canvasSize.width, newY / canvasSize.height)
                        }

                        when {
                            (pos - p0).getDistance() < threshold -> {
                                p0Norm = updatePoint(p0, dragAmount)
                                change.consume()
                            }
                            (pos - p1).getDistance() < threshold -> {
                                p1Norm = updatePoint(p1, dragAmount)
                                change.consume()
                            }
                            (pos - p2).getDistance() < threshold -> {
                                p2Norm = updatePoint(p2, dragAmount)
                                change.consume()
                            }
                            isCubic && (pos - p3).getDistance() < threshold -> {
                                p3Norm = updatePoint(p3, dragAmount)
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                canvasSize = size
                val w = size.width
                val h = size.height

                val p0 = Offset(p0Norm.x * w, p0Norm.y * h)
                val p1 = Offset(p1Norm.x * w, p1Norm.y * h)
                val p2 = Offset(p2Norm.x * w, p2Norm.y * h)
                val p3 = Offset(p3Norm.x * w, p3Norm.y * h)

                val path = Path().apply {
                    moveTo(p0.x, p0.y)
                    if (isCubic) {
                        cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
                    } else {
                        quadraticTo(p1.x, p1.y, p2.x, p2.y)
                    }
                }

                if (isCubic) {
                    drawLine(Color.LightGray, p0, p1, 2f)
                    drawLine(Color.LightGray, p1, p2, 2f)
                    drawLine(Color.LightGray, p2, p3, 2f)
                } else {
                    drawLine(Color.LightGray, p0, p1, 2f)
                    drawLine(Color.LightGray, p1, p2, 2f)
                }

                drawPath(path, Color.Blue, style = Stroke(8f))

                fun drawPointLabel(offset: Offset, label: String, color: Color) {
                    drawCircle(color, 12f, offset)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = label,
                        topLeft = offset + Offset(15f, -40f),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    )
                }

                drawPointLabel(p0, "P0", Color.Black)
                drawPointLabel(p1, "P1", Color.Red)
                if (isCubic) {
                    drawPointLabel(p2, "P2", Color.Magenta)
                    drawPointLabel(p3, "P3", Color.Black)
                } else {
                    drawPointLabel(p2, "P2", Color.Black)
                }
            }
        }
    }
}

@Composable
fun FigmaDeepDiveSection() {
    Text(
        text = "Figma to Compose: Advanced Mapping",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DeepDiveItem(
            title = "1. The Anatomy of a Segment",
            description = "Figma vector 'Segments' consist of an Anchor and two Handles. " +
                          "A single Cubic Bezier in Compose (`cubicTo`) spans between TWO Anchors. " +
                          "Handle 1 of Anchor A and Handle 2 of Anchor B are your Control Points (P1, P2)."
        )
        DeepDiveItem(
            title = "2. Concatenation (Chaining)",
            description = "Complex curves like 'S-waves' are just multiple `cubicTo` calls. " +
                          "The End Point (P3) of the first segment automatically becomes the Start Point (P0) of the next segment."
        )
        DeepDiveItem(
            title = "3. Smoothness Rule",
            description = "To keep a curve smooth in Figma, handles must be collinear. In code, this means the vector from P2 to P3 of segment 1 must be in the same direction as P3 to P1 of segment 2."
        )
    }
}

@Composable
fun DeepDiveItem(title: String, description: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ComplexFigmaExample() {
    var showSpecs by remember { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Complex Wave (Multi-segment)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Show Specs", style = MaterialTheme.typography.labelSmall)
                Checkbox(checked = showSpecs, onCheckedChange = { showSpecs = it })
            }
        }
        
        Text(
            text = "This S-Curve uses two `cubicTo` segments. The red dots are the Figma 'Handles' (Control Points).",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF5F5F5), shape = MaterialTheme.shapes.medium)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Segment 1 (Left to Middle)
                val s1_p0 = Offset(0f, h * 0.5f)
                val s1_p1 = Offset(w * 0.25f, h * 0.1f) // Handle 1
                val s1_p2 = Offset(w * 0.25f, h * 0.9f) // Handle 2
                val s1_p3 = Offset(w * 0.5f, h * 0.5f)  // Middle Anchor

                // Segment 2 (Middle to Right)
                val s2_p1 = Offset(w * 0.75f, h * 0.1f) // Handle 1
                val s2_p2 = Offset(w * 0.75f, h * 0.9f) // Handle 2
                val s2_p3 = Offset(w, h * 0.5f)         // End Anchor

                val path = Path().apply {
                    moveTo(s1_p0.x, s1_p0.y)
                    cubicTo(s1_p1.x, s1_p1.y, s1_p2.x, s1_p2.y, s1_p3.x, s1_p3.y)
                    cubicTo(s2_p1.x, s2_p1.y, s2_p2.x, s2_p2.y, s2_p3.x, s2_p3.y)
                }

                drawPath(path, Color(0xFF1976D2), style = Stroke(width = 6f))

                if (showSpecs) {
                    // Draw Handles (Red) and Helper Lines
                    fun drawHandle(start: Offset, handle: Offset) {
                        drawLine(Color.Red.copy(alpha = 0.5f), start, handle, 2f)
                        drawCircle(Color.Red, 8f, handle)
                    }
                    
                    // Segment 1 Specs
                    drawCircle(Color.Black, 10f, s1_p0)
                    drawHandle(s1_p0, s1_p1)
                    drawHandle(s1_p3, s1_p2)
                    
                    // Segment 2 Specs
                    drawCircle(Color.Black, 10f, s1_p3)
                    drawHandle(s1_p3, s2_p1)
                    drawHandle(s2_p3, s2_p2)
                    drawCircle(Color.Black, 10f, s2_p3)
                }
            }
        }
    }
}

@Composable
fun FigmaToComposeSection() {
    Text(
        text = "Practical Example: Curved Header",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Figma Spec: A 120dp high header. Bottom edge dips 40dp lower at the center.",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(16.dp))

    val CurvedHeaderShape = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height * 0.75f) 
        
        quadraticTo(
            x1 = size.width / 2f,
            y1 = size.height,
            x2 = 0f,
            y2 = size.height * 0.75f
        )
        close()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(CurvedHeaderShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3))))
            .padding(24.dp)
    ) {
        Text("Responsive Header", color = Color.White, style = MaterialTheme.typography.headlineSmall)
    }
}

@Preview(showBackground = true)
@Composable
fun BezierCurveSamplePreview() {
    MaterialTheme {
        BezierCurveSample(onBack = {})
    }
}
