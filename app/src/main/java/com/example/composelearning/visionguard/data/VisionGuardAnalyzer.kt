package com.example.composelearning.visionguard.data

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.composelearning.visionguard.presentation.VisionGuardContract.SpatialTarget

class VisionGuardAnalyzer(
    private val onResult: (List<SpatialTarget>) -> Unit
) : ImageAnalysis.Analyzer {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.close()
    }
}
