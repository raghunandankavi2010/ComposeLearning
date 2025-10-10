package com.example.composelearning

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.composelearning.ui.theme.ComposeLearningTheme
import de.drick.compose.edgetoedgepreviewlib.EdgeToEdgeTemplate
import de.drick.compose.edgetoedgepreviewlib.NavigationMode
import androidx.compose.ui.graphics.Color as ComposeColor

// MainActivity remains the same

private class CollapsingAppBarNestedScrollConnection(
    val appBarMaxHeight: Int
) : NestedScrollConnection {

    var appBarOffset: Int by mutableIntStateOf(0)
        private set

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y.toInt()
        val newOffset = appBarOffset + delta
        val previousOffset = appBarOffset
        appBarOffset = newOffset.coerceIn(-appBarMaxHeight, 0)
        val consumed = appBarOffset - previousOffset
        return Offset(0f, consumed.toFloat())
    }
}

class ListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            )
        )

        setContent {
            val view = LocalView.current
            val isLightMode = true
            SideEffect {
                val window = (view.context as Activity).window
                val insetsController = WindowCompat.getInsetsController(window, view)

                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

                insetsController.isAppearanceLightStatusBars = isLightMode
                insetsController.isAppearanceLightNavigationBars = isLightMode
            }

            ComposeLearningTheme {
                InboxScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen() {

    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val appBarHeight = 65.dp
    val appBarMaxHeightPx = with(density) {
        (appBarHeight + statusBarHeight).roundToPx()
    }

    val connection = remember(appBarMaxHeightPx) {
        CollapsingAppBarNestedScrollConnection(appBarMaxHeightPx)
    }

    val appBarMaxHeightDP = with(density) {
        (appBarHeight.roundToPx() + connection.appBarOffset).toDp()
    }

    Box(
        modifier = Modifier
            .nestedScroll(connection)
            .fillMaxSize()
    ) {
        GeneralAlertsList(
            modifier = Modifier
                .fillMaxSize(),
            lazyListState = lazyListState,
            toolbarHeight = appBarMaxHeightDP

        )

        TopAppBar(
            title = { Text("Inbox") },
            navigationIcon = {
                IconButton(onClick = { /* Handle back */ }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Handle search */ }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ComposeColor.White,
                titleContentColor = ComposeColor.Black,
                navigationIconContentColor = ComposeColor.Black,
                actionIconContentColor = ComposeColor.Black,
            ),
            modifier = Modifier
                .offset { IntOffset(0, connection.appBarOffset) }
                .fillMaxWidth()
        )
        val dummyPadding = with(density) {
            (16).toDp()
        }

//        Spacer(
//            modifier = Modifier
//                .height(statusBarHeight + dummyPadding)
//                .fillMaxWidth()
//                .background(ComposeColor.White)
//        )
    }

}

// GeneralAlertsList and MainActivity remain the same
@Composable
fun GeneralAlertsList(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    toolbarHeight: Dp = 0.dp
) {
    val items = remember {
        (0..50).map { "Inbox Item $it" }
    }

    // Get the default system bar padding values.
    val systemBarPadding = WindowInsets.systemBars.asPaddingValues()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ComposeColor.White),
        state = lazyListState,
        contentPadding = PaddingValues(
            top = systemBarPadding.calculateTopPadding() + toolbarHeight,
            bottom = systemBarPadding.calculateBottomPadding()
        )
    ) {

        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = ComposeColor.Blue)
            ) {
                Text(
                    text = item,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewEdgeToEdge() {
    EdgeToEdgeTemplate(
        isInvertedOrientation = true,
        navMode = NavigationMode.ThreeButton
    ) {
        ComposeLearningTheme {
            InboxScreen()
        }
    }
}

