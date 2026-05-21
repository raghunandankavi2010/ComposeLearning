package com.example.composelearning.tabs

import android.os.Parcelable
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.example.composelearning.animcompose.Navigator
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// ---------- Routes ----------

@Serializable
@Parcelize
sealed interface TabsScreen : NavKey, Parcelable {
    @Serializable @Parcelize data object Photos : TabsScreen
    @Serializable @Parcelize data object Articles : TabsScreen
    @Serializable @Parcelize data object Profile : TabsScreen

    @Serializable @Parcelize data class PhotoDetail(val id: String) : TabsScreen
    @Serializable @Parcelize data class ArticleDetail(val id: String) : TabsScreen
    @Serializable @Parcelize data class SettingDetail(val key: String) : TabsScreen
}

internal val TopLevelTabs: List<TabsScreen> =
    listOf(TabsScreen.Photos, TabsScreen.Articles, TabsScreen.Profile)

internal fun TabsScreen.isTopLevel(): Boolean = this in TopLevelTabs

internal fun TabsScreen.tabTitle(): String = when (this) {
    TabsScreen.Photos -> "Photos"
    TabsScreen.Articles -> "Articles"
    TabsScreen.Profile -> "Profile"
    else -> ""
}

// ---------- Photos tab ----------

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PhotosTabScreen(navigator: Navigator, sharedScope: SharedTransitionScope) {
    val photos = remember { SampleData.photos }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TabHeader("Photos", "Pinch into any frame")
        }
        items(items = photos, key = { it.id }) { photo ->
            PhotoCard(
                photo = photo,
                sharedScope = sharedScope,
                onClick = { navigator.navigate(TabsScreen.PhotoDetail(photo.id)) },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PhotoCard(
    photo: PhotoData,
    sharedScope: SharedTransitionScope,
    onClick: () -> Unit,
) {
    with(sharedScope) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .sharedElement(
                    sharedContentState = rememberSharedContentState("photo-${photo.id}"),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                )
                .clip(RoundedCornerShape(20.dp))
                .background(photo.brush)
                .clickable(onClick = onClick),
        ) {
            Text(
                text = photo.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PhotoDetailScreen(
    id: String,
    sharedScope: SharedTransitionScope,
    onBack: () -> Unit,
) {
    val photo = remember(id) { SampleData.photos.first { it.id == id } }
    with(sharedScope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState("photo-${photo.id}"),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    )
                    .background(photo.brush),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f)),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = photo.title,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = photo.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

// ---------- Articles tab ----------

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ArticlesTabScreen(navigator: Navigator, sharedScope: SharedTransitionScope) {
    val articles = remember { SampleData.articles }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { TabHeader("Articles", "Tap to read full story") }
        items(items = articles, key = { it.id }) { article ->
            ArticleCard(
                article = article,
                sharedScope = sharedScope,
                onClick = { navigator.navigate(TabsScreen.ArticleDetail(article.id)) },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ArticleCard(
    article: ArticleData,
    sharedScope: SharedTransitionScope,
    onClick: () -> Unit,
) {
    with(sharedScope) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState("article-bg-${article.id}"),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                )
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState("article-img-${article.id}"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(article.brush),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState("article-title-${article.id}"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        ),
                    )
                    Text(
                        text = article.preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ArticleDetailScreen(
    id: String,
    sharedScope: SharedTransitionScope,
    onBack: () -> Unit,
) {
    val article = remember(id) { SampleData.articles.first { it.id == id } }
    with(sharedScope) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState("article-bg-${article.id}"),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState("article-img-${article.id}"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        )
                        .background(article.brush),
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(top = 24.dp, start = 12.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.25f)),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = article.title,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState("article-title-${article.id}"),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            ),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = article.body,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

// ---------- Profile tab ----------

@Composable
fun ProfileTabScreen(navigator: Navigator) {
    val settings = remember { SampleData.settings }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 16.dp),
    ) {
        item { TabHeader("Settings", "Account & App preferences") }
        items(items = settings, key = { it.key }) { setting ->
            SettingItem(
                setting = setting,
                onClick = { navigator.navigate(TabsScreen.SettingDetail(setting.key)) },
            )
        }
    }
}

@Composable
private fun SettingItem(setting: SettingData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = setting.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = setting.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SettingDetailScreen(
    key: String,
    onBack: () -> Unit,
) {
    val setting = remember(key) { SampleData.settings.first { it.key == key } }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text(
                text = setting.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = setting.body,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun TabHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------- Sample data ----------

internal data class PhotoData(
    val id: String,
    val title: String,
    val description: String,
    val brush: Brush,
)

internal data class ArticleData(
    val id: String,
    val title: String,
    val preview: String,
    val body: String,
    val brush: Brush,
)

internal data class SettingData(
    val key: String,
    val title: String,
    val subtitle: String,
    val body: String,
)

internal object SampleData {
    val photos = listOf(
        PhotoData("p1", "Sunset", "Coastal sunset, late October.",
            Brush.linearGradient(listOf(Color(0xFFFF512F), Color(0xFFF09819)))),
        PhotoData("p2", "Aurora", "Iceland, around midnight.",
            Brush.linearGradient(listOf(Color(0xFF134E5E), Color(0xFF71B280)))),
        PhotoData("p3", "Neon", "Shibuya crossing, 9 PM.",
            Brush.linearGradient(listOf(Color(0xFFEE0979), Color(0xFFFF6A00)))),
        PhotoData("p4", "Glacier", "Patagonia, in summer.",
            Brush.linearGradient(listOf(Color(0xFF2980B9), Color(0xFF6DD5FA)))),
        PhotoData("p5", "Forest", "Hokkaido, in winter.",
            Brush.linearGradient(listOf(Color(0xFF0F2027), Color(0xFF2C5364)))),
        PhotoData("p6", "Reef", "Komodo, low tide.",
            Brush.linearGradient(listOf(Color(0xFF614385), Color(0xFF516395)))),
    )

    val articles = listOf(
        ArticleData("a1",
            "Per-tab back stacks in Nav3",
            "How each tab maintains an independent history without multiple NavHosts.",
            "In Navigation 3 the back stack lives in a state holder. " +
                "Switch tabs and your nested journey on each tab is preserved — " +
                "no extra NavHost required. The single NavDisplay renders whichever " +
                "stack the current tab points to.",
            Brush.linearGradient(listOf(Color(0xFF4FC3F7), Color(0xFF1976D2)))),
        ArticleData("a2",
            "Shared element transitions, briefly",
            "SharedTransitionLayout + LocalNavAnimatedContentScope is enough.",
            "Wrap NavDisplay in SharedTransitionLayout. In every entry, read " +
                "LocalNavAnimatedContentScope.current and pass it as the " +
                "animatedVisibilityScope for Modifier.sharedElement / sharedBounds. " +
                "Match content via rememberSharedContentState(key).",
            Brush.linearGradient(listOf(Color(0xFFFF7043), Color(0xFFFFB300)))),
        ArticleData("a3",
            "Hiding the bottom bar on detail",
            "Read the current stack depth instead of the route.",
            "currentStack.size == 1 means you're on a top-level destination — show " +
                "the bottom bar. Anything deeper is a detail screen — hide it. This " +
                "avoids hard-coding lists of 'is this a tab root?' routes.",
            Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)))),
    )

    val settings = listOf(
        SettingData("acct", "Account", "Email, password, sign out",
            "Per-tab back stack demo: navigate here, then switch tabs and come back — you'll land right back on this detail screen."),
        SettingData("notif", "Notifications", "Push, email, in-app",
            "The bottom bar disappears on this screen because the current tab's stack depth is > 1."),
        SettingData("priv", "Privacy", "Data, tracking, ads",
            "All three details (Photo, Article, Setting) share the same NavDisplay; only the bottom bar visibility changes."),
        SettingData("about", "About", "Version, licenses",
            "Built with Navigation 3 (NavDisplay + per-tab NavBackStack) and SharedTransitionLayout."),
    )
}
