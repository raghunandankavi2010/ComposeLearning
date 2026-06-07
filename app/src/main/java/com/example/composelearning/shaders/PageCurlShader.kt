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
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult

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
    LaunchedEffect(Unit) {
        val loader = coil.ImageLoader(context)
        
        val frontResult = loader.execute(ImageRequest.Builder(context).data(frontPageUrl).build())
        if (frontResult is SuccessResult) frontBitmap = frontResult.drawable.let { (it as android.graphics.drawable.BitmapDrawable).bitmap }
        
        val backResult = loader.execute(ImageRequest.Builder(context).data(backPageUrl).build())
        if (backResult is SuccessResult) backBitmap = backResult.drawable.let { (it as android.graphics.drawable.BitmapDrawable).bitmap }
    }

    // 2. Interaction State
    var clickPos by remember { mutableStateOf(Offset(0f, 0f)) }
    var dragPos by remember { mutableStateOf(Offset(0f, 0f)) }
    var isDragging by remember { mutableStateOf(false) }

    val shaderCode = """
        uniform vec2 u_resolution;
        uniform vec2 u_click;
        uniform vec2 u_drag;
        uniform shader u_front_tex;
        uniform shader u_back_tex;
        
        const float PI = 3.14159265359;
        const float RADIUS = 0.12; // Cylinder radius

        half4 main(float2 fragCoord) {
            float aspect = u_resolution.x / u_resolution.y;
            vec2 uv = fragCoord / u_resolution;
            
            // Normalize interaction points to UV space with aspect correction
            vec2 click = u_click / u_resolution;
            vec2 drag = u_drag / u_resolution;
            
            // Vector from start to current drag
            vec2 dragVec = drag - click;
            float dragLen = length(dragVec);
            
            if (dragLen < 0.01) {
                return u_front_tex.eval(fragCoord);
            }

            vec2 mouseDir = normalize(dragVec);
            
            // The "Origin" of the fold is calculated to align with the drag
            // In this version, we treat the click as the anchor and the drag as the pull
            // The fold line is perpendicular to mouseDir.
            
            // Project fragment onto the drag vector
            // Dist 0 is at the fold line.
            float mouseDist = dragLen; 
            float proj = dot(uv - click, mouseDir);
            float dist = proj - mouseDist;

            vec2 linePoint = uv - dist * mouseDir;
            
            if (dist > RADIUS) {
                // Next Page (Below the curl)
                // We add a shadow based on distance to the curl
                half4 col = u_back_tex.eval(fragCoord);
                float shadow = pow(clamp(dist - RADIUS, 0.0, 1.0) * 1.5, 0.2);
                return half4(col.rgb * shadow, col.a);
            } 
            else if (dist >= 0.0) {
                // The Cylinder (The Curl)
                float theta = asin(dist / RADIUS);
                
                // Unroll the cylinder to find where this pixel maps on the flat page
                vec2 p2 = linePoint + mouseDir * (PI - theta) * RADIUS; // Back of the page
                vec2 p1 = linePoint + mouseDir * theta * RADIUS;        // Front of the page
                
                // If the back-mapped UV is in bounds, show the back side
                if (p2.x <= 1.0 && p2.y <= 1.0 && p2.x >= 0.0 && p2.y >= 0.0) {
                    half4 col = u_front_tex.eval(p2 * u_resolution);
                    // Light drop-off for realism
                    float lighting = pow(clamp((RADIUS - dist) / RADIUS, 0.0, 1.0), 0.2);
                    // Add a specular highlight at the cylinder peak
                    float highlight = exp(-pow(dist - RADIUS * 0.5, 2.0) * 1000.0) * 0.15;
                    return half4(col.rgb * lighting + highlight, col.a);
                } else {
                    // Otherwise show the front side
                    return u_front_tex.eval(p1 * u_resolution);
                }
            } 
            else {
                // The Flipped Page (Now laying flat on top)
                vec2 p = linePoint + mouseDir * (abs(dist) + PI * RADIUS);
                if (p.x <= 1.0 && p.y <= 1.0 && p.x >= 0.0 && p.y >= 0.0) {
                    return u_front_tex.eval(p * u_resolution);
                } else {
                    return u_back_tex.eval(fragCoord);
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
                    runtimeShader.setFloatUniform("u_click", clickPos.x, clickPos.y)
                    runtimeShader.setFloatUniform("u_drag", dragPos.x, dragPos.y)
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
