package com.example.composelearning.shaders

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import coil3.SingletonImageLoader
import coil3.toBitmap
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware

/**
 * AGSL Page Curl Implementation
 *
 * This shader simulates a cylindrical page curl. It takes two textures:
 * 1. Current Page (The one being curled)
 * 2. Next Page (The one revealed underneath)
 */
@Composable
fun PageCurlShaderScreen() {
    val context = LocalContext.current

    // 1. Load Dummy Images
    val frontPageUrl = "https://picsum.photos/seed/page1/1080/1920"
    val backPageUrl = "https://picsum.photos/seed/page2/1080/1920"

    var frontBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var backBitmap by remember { mutableStateOf<Bitmap?>(null) }


    // Fetch bitmaps
    LaunchedEffect(frontPageUrl, backPageUrl) {
        val loader = SingletonImageLoader.get(context)

        // 1. Request the front image
        val frontRequest = ImageRequest.Builder(context)
            .data(frontPageUrl)
            .allowHardware(false) // Required to prevent GPU-only memory locking
            .build()

        val frontResult = loader.execute(frontRequest)
        if (frontResult is SuccessResult) {
            frontBitmap = frontResult.image.toBitmap()
        }

        // 2. Request the back image
        val backRequest = ImageRequest.Builder(context)
            .data(backPageUrl)
            .allowHardware(false)
            .build()

        val backResult = loader.execute(backRequest)
        if (backResult is SuccessResult) {
            backBitmap = backResult.image.toBitmap()
        }
    }

    // 2. Interaction State
    var clickPos by remember { mutableStateOf(Offset(0f, 0f)) }
    var dragPos by remember { mutableStateOf(Offset(0f, 0f)) }
    var isDragging by remember { mutableStateOf(false) }

    val shaderCode = """
        uniform float2 u_resolution;
        uniform float2 u_mouse; // Current drag position (dragPos)
        uniform float2 u_drag_start; // Initial touch point (clickPos)
        uniform shader u_front_tex;
        uniform shader u_back_tex;

        const float PI = 3.14159265359;
        const float RADIUS = 0.1;

        // Helper to convert our aspect-corrected space back to 0..1 UV for sampling
        float2 sampleUV(float2 p, float asp) {
            return p * float2(1.0 / asp, 1.0);
        }

        half4 main(float2 fragCoord) {
            float aspect = u_resolution.x / u_resolution.y;

            // Map all coordinates to a space where Y is 0..1 and X is 0..aspect
            float2 uv = (fragCoord / u_resolution.xy) * float2(aspect, 1.0);
            float2 mouse = (u_mouse / u_resolution.xy) * float2(aspect, 1.0);
            float2 dragStart = (u_drag_start / u_resolution.xy) * float2(aspect, 1.0);

            // 1. Calculate the fold orientation (mouseDir)
            // It uses the vector between the start and current point, but absolute-values the start
            // to find a stable anchor point (origin).
            float2 mouseDir = normalize(abs(dragStart) - mouse);

            // 2. Calculate the Origin
            // This anchors the fold to the screen edge based on the drag direction
            float2 origin = clamp(mouse - mouseDir * mouse.x / mouseDir.x, 0.0, 1.0);
            if (mouseDir.x < 0.0) {
                 // Adjust origin if dragging in the other direction
                 origin = clamp(mouse - mouseDir * (mouse.x - aspect) / mouseDir.x, 0.0, aspect);
            }

            // 3. Calculate distance from mouse to origin (the "fold depth")
            float mouseDist = length(mouse - origin);

            // 4. Project fragment onto the curl axis
            float proj = dot(uv - origin, mouseDir);
            float dist = proj - mouseDist;

            float2 linePoint = uv - dist * mouseDir;

            if (dist > RADIUS) {
                // Next Page (Revealed underneath)
                half4 col = u_back_tex.eval(sampleUV(uv, aspect) * u_resolution);
                // Shadow based on distance from the cylinder
                float shadow = pow(clamp(dist - RADIUS, 0.0, 1.0) * 1.5, 0.2);
                return half4(col.rgb * shadow, col.a);
            }
            else if (dist >= 0.0) {
                // The Cylinder Surface
                float theta = asin(dist / RADIUS);
                float2 p2 = linePoint + mouseDir * (PI - theta) * RADIUS;
                float2 p1 = linePoint + mouseDir * theta * RADIUS;

                // Back of the page check
                if (p2.x <= aspect && p2.y <= 1.0 && p2.x >= 0.0 && p2.y >= 0.0) {
                    half4 col = u_front_tex.eval(sampleUV(p2, aspect) * u_resolution);
                    float lighting = pow(clamp((RADIUS - dist) / RADIUS, 0.0, 1.0), 0.2);
                    return half4(col.rgb * lighting, col.a);
                } else {
                    // Front side
                    return u_front_tex.eval(sampleUV(p1, aspect) * u_resolution);
                }
            }
            else {
                // The Flipped Flat Page
                float2 p = linePoint + mouseDir * (abs(dist) + PI * RADIUS);
                if (p.x <= aspect && p.y <= 1.0 && p.x >= 0.0 && p.y >= 0.0) {
                    return u_front_tex.eval(sampleUV(p, aspect) * u_resolution);
                } else {
                    return u_front_tex.eval(sampleUV(uv, aspect) * u_resolution);
                }
            }
        }
    """.trimIndent()

    val runtimeShader = remember { RuntimeShader(shaderCode) }

    if (frontBitmap != null && backBitmap != null) {
        val frontShader = remember(frontBitmap) { BitmapShader(frontBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
        val backShader = remember(backBitmap) { BitmapShader(backBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            clickPos = offset
                            dragPos = offset
                            isDragging = true
                        },
                        onDrag = { change, _ ->
                            dragPos = change.position
                        },
                        onDragEnd = {
                            isDragging = false
                            // Optional: Animate back to original or complete the flip
                        }
                    )
                }
                .drawWithCache {
                    runtimeShader.setFloatUniform("u_resolution", size.width, size.height)
                    runtimeShader.setFloatUniform("u_mouse", dragPos.x, dragPos.y)
                    runtimeShader.setFloatUniform("u_drag_start", clickPos.x, clickPos.y)
                    runtimeShader.setInputShader("u_front_tex", frontShader)
                    runtimeShader.setInputShader("u_back_tex", backShader)

                    onDrawWithContent {
                        drawRect(brush = androidx.compose.ui.graphics.ShaderBrush(runtimeShader))
                    }
                }
        )
    } else {
        // Loading state
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
}
