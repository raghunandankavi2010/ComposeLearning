package com.example.composelearning.docintel.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

object DocIntelContract {

    enum class DocType(val label: String) {
        RECEIPT("Receipt / Invoice"),
        ID_CARD("Identity / License"),
        NUTRITION_LABEL("Nutrition Facts"),
        TECHNICAL_CODE("Source Code / Spec"),
        GENERAL_DOCUMENT("General Document")
    }

    enum class FieldCategory {
        TOTAL_AMOUNT,
        DATE,
        VENDOR_NAME,
        ADDRESS,
        DOCUMENT_ID,
        LINE_ITEM,
        TRANSLATION,
        UNCLASSIFIED
    }

    @Immutable
    data class SpatialTextBlock(
        val id: String,
        val text: String,
        val boundingBox: Rect, // Normalized 0..1 bounding box
        val category: FieldCategory,
        val translatedText: String? = null,
        val confidence: Float = 0.92f
    )

    @Immutable
    data class DocumentSummary(
        val docType: DocType,
        val title: String,
        val keyFields: Map<String, String>,
        val totalAmount: String? = null,
        val detectedLanguage: String = "English",
        val aiInsights: List<String>
    )

    @Immutable
    data class State(
        val isCameraPermissionGranted: Boolean = false,
        val isSimulationMode: Boolean = true,
        val docType: DocType = DocType.RECEIPT,
        val textBlocks: List<SpatialTextBlock> = emptyMap<String, SpatialTextBlock>().values.toList(),
        val selectedBlockId: String? = null,
        val summary: DocumentSummary? = null,
        val searchQuery: String = "",
        val aiAnswer: String? = null
    )

    sealed interface Intent {
        data class PermissionResult(val isGranted: Boolean) : Intent
        data class SelectDocType(val type: DocType) : Intent
        data class SelectBlock(val blockId: String?) : Intent
        data class SearchQueryChanged(val query: String) : Intent
        data object AskAiQuestion : Intent
        data object ToggleSimulationMode : Intent
    }
}
