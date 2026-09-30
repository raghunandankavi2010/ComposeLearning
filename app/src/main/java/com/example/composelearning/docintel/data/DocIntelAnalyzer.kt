package com.example.composelearning.docintel.data

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Rect
import com.example.composelearning.docintel.presentation.DocIntelContract.DocType
import com.example.composelearning.docintel.presentation.DocIntelContract.FieldCategory
import com.example.composelearning.docintel.presentation.DocIntelContract.SpatialTextBlock

class DocIntelAnalyzer(
    private val onResult: (List<SpatialTextBlock>) -> Unit
) : ImageAnalysis.Analyzer {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        // Frame analysis close
        imageProxy.close()
    }

    companion object {
        fun generateSampleDocumentData(type: DocType): List<SpatialTextBlock> {
            return when (type) {
                DocType.RECEIPT -> listOf(
                    SpatialTextBlock(
                        id = "vendor",
                        text = "BLUE BOTTLE COFFEE",
                        boundingBox = Rect(0.18f, 0.12f, 0.82f, 0.19f),
                        category = FieldCategory.VENDOR_NAME,
                        translatedText = "Blue Bottle Cafe"
                    ),
                    SpatialTextBlock(
                        id = "date",
                        text = "2026-04-12 09:41 AM",
                        boundingBox = Rect(0.20f, 0.22f, 0.80f, 0.27f),
                        category = FieldCategory.DATE
                    ),
                    SpatialTextBlock(
                        id = "item1",
                        text = "1x Oat Latte   $6.50",
                        boundingBox = Rect(0.15f, 0.32f, 0.85f, 0.38f),
                        category = FieldCategory.LINE_ITEM
                    ),
                    SpatialTextBlock(
                        id = "item2",
                        text = "1x Avocado Toast  $14.00",
                        boundingBox = Rect(0.15f, 0.40f, 0.85f, 0.46f),
                        category = FieldCategory.LINE_ITEM
                    ),
                    SpatialTextBlock(
                        id = "tax",
                        text = "Tax (9.25%): $1.90",
                        boundingBox = Rect(0.15f, 0.52f, 0.85f, 0.57f),
                        category = FieldCategory.UNCLASSIFIED
                    ),
                    SpatialTextBlock(
                        id = "total",
                        text = "TOTAL: $22.40",
                        boundingBox = Rect(0.15f, 0.60f, 0.85f, 0.68f),
                        category = FieldCategory.TOTAL_AMOUNT,
                        translatedText = "TOTAL DUE: $22.40 USD"
                    )
                )

                DocType.ID_CARD -> listOf(
                    SpatialTextBlock(
                        id = "id_title",
                        text = "STATE DRIVER LICENSE",
                        boundingBox = Rect(0.15f, 0.15f, 0.85f, 0.22f),
                        category = FieldCategory.DOCUMENT_ID
                    ),
                    SpatialTextBlock(
                        id = "name",
                        text = "NAME: ALEX R. DEV",
                        boundingBox = Rect(0.35f, 0.28f, 0.90f, 0.35f),
                        category = FieldCategory.VENDOR_NAME
                    ),
                    SpatialTextBlock(
                        id = "dob",
                        text = "DOB: 1995-08-24",
                        boundingBox = Rect(0.35f, 0.38f, 0.90f, 0.44f),
                        category = FieldCategory.DATE
                    ),
                    SpatialTextBlock(
                        id = "lic_num",
                        text = "LIC #: D98471029",
                        boundingBox = Rect(0.35f, 0.48f, 0.90f, 0.55f),
                        category = FieldCategory.DOCUMENT_ID
                    )
                )

                DocType.NUTRITION_LABEL -> listOf(
                    SpatialTextBlock(
                        id = "nut_title",
                        text = "Nutrition Facts",
                        boundingBox = Rect(0.20f, 0.12f, 0.80f, 0.18f),
                        category = FieldCategory.UNCLASSIFIED
                    ),
                    SpatialTextBlock(
                        id = "calories",
                        text = "Calories: 230 kcal",
                        boundingBox = Rect(0.18f, 0.24f, 0.82f, 0.31f),
                        category = FieldCategory.LINE_ITEM
                    ),
                    SpatialTextBlock(
                        id = "protein",
                        text = "Protein: 18g (36% DV)",
                        boundingBox = Rect(0.18f, 0.35f, 0.82f, 0.42f),
                        category = FieldCategory.LINE_ITEM
                    ),
                    SpatialTextBlock(
                        id = "allergens",
                        text = "CONTAINS: Milk, Soy, Tree Nuts",
                        boundingBox = Rect(0.15f, 0.50f, 0.85f, 0.58f),
                        category = FieldCategory.UNCLASSIFIED,
                        translatedText = "ALLERGEN WARNING: Milk & Nuts"
                    )
                )

                else -> listOf(
                    SpatialTextBlock(
                        id = "gen_1",
                        text = "On-Device Neural Document Parsing",
                        boundingBox = Rect(0.15f, 0.20f, 0.85f, 0.28f),
                        category = FieldCategory.UNCLASSIFIED
                    ),
                    SpatialTextBlock(
                        id = "gen_2",
                        text = "Edge AI extracts key-value entities in real-time.",
                        boundingBox = Rect(0.15f, 0.32f, 0.85f, 0.40f),
                        category = FieldCategory.UNCLASSIFIED
                    )
                )
            }
        }
    }
}
