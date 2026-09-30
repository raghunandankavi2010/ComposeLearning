package com.example.composelearning.docintel.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.composelearning.docintel.presentation.DocIntelContract.FieldCategory
import com.example.composelearning.docintel.presentation.DocIntelContract.SpatialTextBlock

@Composable
fun DocIntelOverlay(
    textBlocks: List<SpatialTextBlock>,
    selectedBlockId: String?,
    onBlockSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(textBlocks) {
                detectTapGestures { tapOffset ->
                    val w = size.width
                    val h = size.height

                    val tappedBlock = textBlocks.firstOrNull { block ->
                        val rect = block.boundingBox
                        val left = rect.left * w
                        val top = rect.top * h
                        val right = rect.right * w
                        val bottom = rect.bottom * h
                        tapOffset.x in left..right && tapOffset.y in top..bottom
                    }
                    onBlockSelected(tappedBlock?.id)
                }
            }
    ) {
        val w = size.width
        val h = size.height

        for (block in textBlocks) {
            val rect = block.boundingBox
            val left = rect.left * w
            val top = rect.top * h
            val rectW = rect.width * w
            val rectH = rect.height * h

            val isSelected = block.id == selectedBlockId
            val color = when (block.category) {
                FieldCategory.TOTAL_AMOUNT -> Color(0xFF00E676)
                FieldCategory.VENDOR_NAME -> Color(0xFF00E5FF)
                FieldCategory.DATE -> Color(0xFFFF9100)
                FieldCategory.DOCUMENT_ID -> Color(0xFFE040FB)
                else -> Color(0xFF80D8FF)
            }

            // Draw bounding box
            drawRoundRect(
                color = if (isSelected) Color.White else color.copy(alpha = 0.3f),
                topLeft = Offset(left, top),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Draw AR stroke outline
            drawRoundRect(
                color = if (isSelected) Color.White else color,
                topLeft = Offset(left, top),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(
                    width = if (isSelected) 6f else 3f,
                    pathEffect = if (isSelected) null else PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )
        }
    }
}
