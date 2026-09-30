package com.example.composelearning.docintel.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.docintel.presentation.DocIntelContract.DocType
import com.example.composelearning.docintel.presentation.DocIntelContract.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocIntelScreen(
    onBack: () -> Unit = {},
    viewModel: DocIntelViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AR Document Intelligence",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Spatial Text Parsing & AI Q&A",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117))
            )
        },
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Document Type Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DocType.entries.toTypedArray()) { docType ->
                    FilterChip(
                        selected = state.docType == docType,
                        onClick = { viewModel.processIntent(Intent.SelectDocType(docType)) },
                        label = { Text(docType.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00E5FF),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF21262D),
                            labelColor = Color.White
                        )
                    )
                }
            }

            // AR Camera / Canvas Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161B22))
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(16.dp))
            ) {
                // AR Spatial Text Overlay
                DocIntelOverlay(
                    textBlocks = state.textBlocks,
                    selectedBlockId = state.selectedBlockId,
                    onBlockSelected = { blockId ->
                        viewModel.processIntent(Intent.SelectBlock(blockId))
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Instruction Overlay
                Text(
                    text = "Tap any highlighted AR box to translate & inspect",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(Color(0xCC0D1117), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Natural Language Q&A & Selected Field Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Selected Block detail if tapped
                val selectedBlock = state.textBlocks.firstOrNull { it.id == state.selectedBlockId }
                if (selectedBlock != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2640)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Selected Field: ${selectedBlock.category.name}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(selectedBlock.text, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                            if (selectedBlock.translatedText != null) {
                                Text("Translation: ${selectedBlock.translatedText}", color = Color(0xFF00E676), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // AI Answer box if present
                if (state.aiAnswer != null) {
                    Surface(
                        color = Color(0xFF1B382B),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(state.aiAnswer!!, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Question Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.processIntent(Intent.SearchQueryChanged(it)) },
                        placeholder = { Text("Ask AI about this document (e.g. Total cost?)", color = Color.Gray, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF30363D),
                            focusedContainerColor = Color(0xFF161B22),
                            unfocusedContainerColor = Color(0xFF161B22),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.processIntent(Intent.AskAiQuestion) },
                        modifier = Modifier
                            .background(Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Ask", tint = Color.Black)
                    }
                }
            }
        }
    }
}
