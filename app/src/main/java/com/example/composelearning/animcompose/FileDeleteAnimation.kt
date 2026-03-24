package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================
// DATA CLASSES
// ============================================

data class FileItem(
    val id: String,
    val name: String,
    val size: String,
    val type: FileType,
    val date: String
)

enum class FileType {
    DOCUMENT, IMAGE, VIDEO, AUDIO, FOLDER, ARCHIVE
}

// ============================================
// MAIN SCREEN WITH WORKING ANIMATION
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen() {
    var files by remember {
        mutableStateOf(
            List(8) { index ->
                FileItem(
                    id = "file_$index",
                    name = listOf("Report.pdf", "Photo.jpg", "Video.mp4", "Music.mp3")[index % 4],
                    size = "${(100..5000).random()} KB",
                    type = FileType.values()[index % 6],
                    date = "2024-03-${10 + index}"
                )
            }
        )
    }

    // Animation states - MUST be remembered properly
    var animatingFileId by remember { mutableStateOf<String?>(null) }
    var animationProgress by remember { mutableFloatStateOf(0f) }
    var isBinOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Files") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Animated Trash Bin
                    TrashBin(
                        isOpen = isBinOpen,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = files,
                    key = { it.id }
                ) { file ->
                    // CRITICAL: Check if this specific file is being animated
                    val isDeleting = animatingFileId == file.id

                    FileItemWithAnimation(
                        file = file,
                        isDeleting = isDeleting,
                        deleteProgress = if (isDeleting) animationProgress else 0f,
                        onLongClick = {
                            // Start deletion animation
                            if (animatingFileId == null) {
                                animatingFileId = file.id
                                isBinOpen = true

                                scope.launch {
                                    // Phase 1: Lift (wait a bit)
                                    delay(200)

                                    // Phase 2: Animate to bin
                                    val anim = Animatable(0f)
                                    anim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 600,
                                            easing = FastOutSlowInEasing
                                        )
                                    ) {
                                        animationProgress = value
                                    }

                                    // Phase 3: Actually remove the file
                                    files = files.filter { it.id != file.id }
                                    animatingFileId = null
                                    animationProgress = 0f
                                    isBinOpen = false

                                    // Show snackbar
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${file.name} moved to trash",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        files = files + file
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ============================================
// FILE ITEM WITH PROPER ANIMATION
// ============================================

@Composable
fun FileItemWithAnimation(
    file: FileItem,
    isDeleting: Boolean,
    deleteProgress: Float,
    onLongClick: () -> Unit
) {
    // Use animateFloatAsState for smooth transitions
    val scale by animateFloatAsState(
        targetValue = if (isDeleting) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isDeleting) 0.3f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // CRITICAL: Use graphicsLayer for the slide animation
    // This is more performant and reliable than offset modifier
    val slideX = deleteProgress * 400f // Slide to the right toward bin

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
                this.translationX = slideX
                this.rotationZ = deleteProgress * -10f // Slight rotation
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeleting)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDeleting) 8.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File Icon
            FileIcon(file.type, isDeleting)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDeleting)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${file.size} • ${file.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete indicator
            if (isDeleting) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Deleting",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ============================================
// TRASH BIN WITH LID ANIMATION
// ============================================

@Composable
fun TrashBin(
    isOpen: Boolean,
    modifier: Modifier = Modifier
) {
    // Animate lid opening
    val lidRotation by animateFloatAsState(
        targetValue = if (isOpen) -50f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "lid"
    )

    // Bin shake when item drops
    val shake by animateFloatAsState(
        targetValue = if (isOpen) 0f else 0f,
        animationSpec = repeatable(
            iterations = if (isOpen) 0 else 3,
            animation = tween(50),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    // Scale pulse when open
    val binScale by animateFloatAsState(
        targetValue = if (isOpen) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "binScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Lid
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = lidRotation
                    transformOrigin = TransformOrigin(0.5f, 1f) // Pivot at bottom
                }
                .width(48.dp)
                .height(10.dp)
                .background(
                    color = if (isOpen) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Bin Body
        Surface(
            shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
            color = if (isOpen) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .size(48.dp, 50.dp)
                .scale(binScale)
                .graphicsLayer {
                    translationX = shake
                }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Trash",
                    tint = if (isOpen) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ============================================
// FILE ICON COMPONENT
// ============================================

@Composable
private fun FileIcon(type: FileType, isDeleting: Boolean) {
    val (icon, containerColor, contentColor) = when (type) {
        FileType.DOCUMENT -> Triple(
            Icons.Default.Description,
            Color(0xFFE3F2FD),
            Color(0xFF1976D2)
        )
        FileType.IMAGE -> Triple(
            Icons.Default.Image,
            Color(0xFFF3E5F5),
            Color(0xFF7B1FA2)
        )
        FileType.VIDEO -> Triple(
            Icons.Default.Videocam,
            Color(0xFFFFEBEE),
            Color(0xFFC62828)
        )
        FileType.AUDIO -> Triple(
            Icons.Default.Audiotrack,
            Color(0xFFE8F5E9),
            Color(0xFF388E3C)
        )
        FileType.FOLDER -> Triple(
            Icons.Default.Folder,
            Color(0xFFFFF3E0),
            Color(0xFFF57C00)
        )
        FileType.ARCHIVE -> Triple(
            Icons.Default.FolderZip,
            Color(0xFFECEFF1),
            Color(0xFF546E7A)
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDeleting) MaterialTheme.colorScheme.errorContainer else containerColor,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDeleting) MaterialTheme.colorScheme.error else contentColor,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// ============================================
// PREVIEW
// ============================================

@Preview(showBackground = true)
@Composable
fun FileManagerPreview() {
    MaterialTheme {
        FileManagerScreen()
    }
}
