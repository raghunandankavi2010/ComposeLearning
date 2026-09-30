package com.example.composelearning.docintel.presentation

import androidx.lifecycle.ViewModel
import com.example.composelearning.docintel.data.DocIntelAnalyzer
import com.example.composelearning.docintel.presentation.DocIntelContract.DocType
import com.example.composelearning.docintel.presentation.DocIntelContract.DocumentSummary
import com.example.composelearning.docintel.presentation.DocIntelContract.FieldCategory
import com.example.composelearning.docintel.presentation.DocIntelContract.Intent
import com.example.composelearning.docintel.presentation.DocIntelContract.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DocIntelViewModel : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadDocumentData(DocType.RECEIPT)
    }

    fun processIntent(intent: Intent) {
        when (intent) {
            is Intent.PermissionResult -> {
                _state.update { it.copy(isCameraPermissionGranted = intent.isGranted) }
            }
            is Intent.SelectDocType -> {
                _state.update { it.copy(docType = intent.type, selectedBlockId = null) }
                loadDocumentData(intent.type)
            }
            is Intent.SelectBlock -> {
                _state.update { it.copy(selectedBlockId = intent.blockId) }
            }
            is Intent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
            is Intent.AskAiQuestion -> {
                generateAiAnswer(_state.value.searchQuery)
            }
            is Intent.ToggleSimulationMode -> {
                _state.update { it.copy(isSimulationMode = !it.isSimulationMode) }
            }
        }
    }

    private fun loadDocumentData(type: DocType) {
        val blocks = DocIntelAnalyzer.generateSampleDocumentData(type)
        val summary = generateSummary(type, blocks)
        _state.update {
            it.copy(
                textBlocks = blocks,
                summary = summary
            )
        }
    }

    private fun generateSummary(type: DocType, blocks: List<DocIntelContract.SpatialTextBlock>): DocumentSummary {
        val keyFields = mutableMapOf<String, String>()
        var totalAmount: String? = null

        for (block in blocks) {
            when (block.category) {
                FieldCategory.TOTAL_AMOUNT -> totalAmount = block.text
                FieldCategory.VENDOR_NAME -> keyFields["Vendor / Subject"] = block.text
                FieldCategory.DATE -> keyFields["Date"] = block.text
                FieldCategory.DOCUMENT_ID -> keyFields["Document ID"] = block.text
                else -> {}
            }
        }

        val insights = when (type) {
            DocType.RECEIPT -> listOf(
                "Expense categorized under 'Food & Beverage'.",
                "Tax portion verified at 9.25%.",
                "Ready for automated expensing export."
            )
            DocType.ID_CARD -> listOf(
                "Driver License valid in State database.",
                "Identity age verified (> 21 years old)."
            )
            DocType.NUTRITION_LABEL -> listOf(
                "High protein density (18g per serving).",
                "Allergen Warning: Contains Milk & Soy."
            )
            else -> listOf("Structured layout mapped successfully.")
        }

        return DocumentSummary(
            docType = type,
            title = when (type) {
                DocType.RECEIPT -> "Cafe & Dining Receipt"
                DocType.ID_CARD -> "Driver Identification Card"
                DocType.NUTRITION_LABEL -> "Food Nutrition Label"
                else -> "Document Scanner"
            },
            keyFields = keyFields,
            totalAmount = totalAmount,
            aiInsights = insights
        )
    }

    private fun generateAiAnswer(query: String) {
        if (query.isBlank()) return
        val currentSummary = _state.value.summary
        val blocks = _state.value.textBlocks

        val answer = when {
            query.contains("total", ignoreCase = true) || query.contains("cost", ignoreCase = true) || query.contains("amount", ignoreCase = true) -> {
                currentSummary?.totalAmount?.let { "The detected total amount is $it." }
                    ?: "No total amount found in document."
            }
            query.contains("date", ignoreCase = true) || query.contains("when", ignoreCase = true) -> {
                currentSummary?.keyFields?.get("Date")?.let { "Document date is $it." }
                    ?: "No explicit date field found."
            }
            query.contains("allergen", ignoreCase = true) || query.contains("contains", ignoreCase = true) -> {
                blocks.firstOrNull { it.text.contains("CONTAINS", ignoreCase = true) }?.text
                    ?: "No allergen warnings detected."
            }
            else -> "Based on spatial document analysis: '${blocks.firstOrNull()?.text ?: "Text parsed"}' extracted successfully."
        }

        _state.update { it.copy(aiAnswer = answer) }
    }
}
