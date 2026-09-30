package com.example.composelearning.gesturenexus.data

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.HandLandmarkPoint
import kotlin.math.cos
import kotlin.math.sin

class GestureNexusAnalyzer(
    private val onResult: (Map<Int, HandLandmarkPoint>?) -> Unit
) : ImageAnalysis.Analyzer {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.close()
    }

    companion object {
        // MediaPipe Hand Landmark Indices
        const val WRIST = 0
        const val THUMB_CMC = 1
        const val THUMB_MCP = 2
        const val THUMB_IP = 3
        const val THUMB_TIP = 4
        const val INDEX_FINGER_MCP = 5
        const val INDEX_FINGER_PIP = 6
        const val INDEX_FINGER_DIP = 7
        const val INDEX_FINGER_TIP = 8
        const val MIDDLE_FINGER_MCP = 9
        const val MIDDLE_FINGER_PIP = 10
        const val MIDDLE_FINGER_DIP = 11
        const val MIDDLE_FINGER_TIP = 12
        const val RING_FINGER_MCP = 13
        const val RING_FINGER_PIP = 14
        const val RING_FINGER_DIP = 15
        const val RING_FINGER_TIP = 16
        const val PINKY_MCP = 17
        const val PINKY_PIP = 18
        const val PINKY_DIP = 19
        const val PINKY_TIP = 20

        fun generateSimulatedHandLandmarks(elapsedTimeMs: Long): Map<Int, HandLandmarkPoint> {
            val t = elapsedTimeMs / 1000f
            val orbitX = 0.5f + 0.25f * cos(t * 1.5f)
            val orbitY = 0.45f + 0.18f * sin(t * 3.0f)

            return mapOf(
                WRIST to HandLandmarkPoint(orbitX, orbitY + 0.25f),
                THUMB_CMC to HandLandmarkPoint(orbitX - 0.08f, orbitY + 0.18f),
                THUMB_MCP to HandLandmarkPoint(orbitX - 0.10f, orbitY + 0.10f),
                THUMB_IP to HandLandmarkPoint(orbitX - 0.08f, orbitY + 0.04f),
                THUMB_TIP to HandLandmarkPoint(orbitX - 0.04f, orbitY + 0.01f),
                INDEX_FINGER_MCP to HandLandmarkPoint(orbitX - 0.03f, orbitY + 0.12f),
                INDEX_FINGER_PIP to HandLandmarkPoint(orbitX - 0.02f, orbitY + 0.06f),
                INDEX_FINGER_DIP to HandLandmarkPoint(orbitX - 0.01f, orbitY + 0.02f),
                INDEX_FINGER_TIP to HandLandmarkPoint(orbitX, orbitY), // Fingertip tip at orbit position
                MIDDLE_FINGER_MCP to HandLandmarkPoint(orbitX + 0.02f, orbitY + 0.12f),
                MIDDLE_FINGER_PIP to HandLandmarkPoint(orbitX + 0.02f, orbitY + 0.05f),
                MIDDLE_FINGER_DIP to HandLandmarkPoint(orbitX + 0.02f, orbitY + 0.01f),
                MIDDLE_FINGER_TIP to HandLandmarkPoint(orbitX + 0.02f, orbitY - 0.02f),
                RING_FINGER_MCP to HandLandmarkPoint(orbitX + 0.06f, orbitY + 0.14f),
                RING_FINGER_PIP to HandLandmarkPoint(orbitX + 0.06f, orbitY + 0.08f),
                RING_FINGER_DIP to HandLandmarkPoint(orbitX + 0.06f, orbitY + 0.04f),
                RING_FINGER_TIP to HandLandmarkPoint(orbitX + 0.06f, orbitY + 0.01f),
                PINKY_MCP to HandLandmarkPoint(orbitX + 0.10f, orbitY + 0.16f),
                PINKY_PIP to HandLandmarkPoint(orbitX + 0.10f, orbitY + 0.11f),
                PINKY_DIP to HandLandmarkPoint(orbitX + 0.10f, orbitY + 0.08f),
                PINKY_TIP to HandLandmarkPoint(orbitX + 0.10f, orbitY + 0.05f)
            )
        }
    }
}
