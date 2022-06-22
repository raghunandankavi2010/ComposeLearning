package com.example.composelearning

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.ui.theme.ComposeLearningTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeLearningTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(text = "Profile List")
                                },
                                navigationIcon = {
                                    IconButton(onClick = { }) {
                                        Icon(Icons.Filled.Menu, "")
                                    }
                                },
                                backgroundColor = Blue,
                                contentColor = White,
                                elevation = 12.dp
                            )
                        }, content = {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                //MainContent()
                                //CustomProgressBar()
                                var progress by remember { mutableStateOf(0f) }
                                MultiColorProgressCanvas()
                                CustomImageSlider(
                                    modifier = Modifier.padding(16.dp),
                                    value = progress,
                                    onValueChange = { value, offset ->
                                        progress = value
                                    },
                                    trackHeight = 10.dp,
                                    colors = MaterialSliderDefaults.materialColors(
                                        inactiveTrackColor = SliderBrushColor(color = Color(0xFFDBDBDB)),
                                        activeTrackColor = SliderBrushColor(
                                            color = Color(0xFF69BA6E)
                                        )
                                    )
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.thumb),
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        })
                }
            }
        }
    }

    @Composable
    fun MainContent() {
        val list = listOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "W", "X", "Y", "Z"
        )
        val currentPosition = remember { mutableStateOf(0) }
        LogCompositions("JetpackCompose.app", "MyComposable function")
        val onClick: (String, Int) -> Unit = { alphabet: String, positionClicked: Int ->
            currentPosition.value = positionClicked
            Toast.makeText(
                this@MainActivity.applicationContext,
                alphabet,
                Toast.LENGTH_SHORT
            ).show()
        }
        ProfileList(list, currentPosition = currentPosition.value, onClick)
    }
}

@Composable
fun ProfileList(list: List<String>, currentPosition: Int, onClick: (String, Int) -> Unit) {
    Column(Modifier.padding(start = 8.dp, top = 20.dp, end = 8.dp)) {
        val countProfileImage = if (list.size > 3) {
            list.size - 3
        } else {
            0
        }
        val toShowList = list.take(4)
        val totalCount = 10
        val listState = rememberLazyListState()
        val firstVisibleItemIndex by remember(listState) {
            derivedStateOf {
                listState.layoutInfo.visibleItemsInfo.first().index
            }
        }
        Text(
            "TEST",
            textAlign = TextAlign.Center,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy((-32).dp)
            ) {
                val firstVisibleItem = listState.firstVisibleItemIndex
                val offSet = listState.firstVisibleItemScrollOffset
                itemsIndexed(toShowList) { index, item ->
                    val alpha = if (currentPosition != index) {
                        1f
                    } else {
                        1f
                    }
                    if (index == 3) {
                        ProfilePictureWithImage(
                            onClick,
                            item,
                            index,
                            count = countProfileImage
                        )
                    } else {
                        ProfilePicture(alpha, onClick, item, index)
                        Spacer(modifier = Modifier.padding(8.dp))
                    }
                }
            }
            if (toShowList.size < totalCount) {
                ShowAddImage()
            }
        }
    }
}

@Composable
fun ShowAddImage() {
    Column(
        modifier = Modifier.height(200.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "Add more",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)                       // clip to the circle shape
                .clickable { }
        )
        Text(
            "Add",
            textAlign = TextAlign.Center,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun ProfilePictureWithImage(
    onClick: (String, Int) -> Unit,
    item: String,
    index: Int,
    count: Int
) {
    LogCompositions("JetpackCompose.app", "Profile Picture")
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background("#F0F5F9".color, CircleShape)
            .clickable { onClick(item, index) },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)                       // clip to the circle shape
                .border(2.dp, Blue, CircleShape)
                .clickable { onClick(item, index) }
        )

        Text(
            "$count",
            color = Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(30.dp)
                .height(30.dp)
                .wrapContentHeight()
        )
    }
}

@Composable
fun ProfilePictureWithCount(
    onClick: (String, Int) -> Unit,
    item: String,
    index: Int,
    count: Int
) {
    LogCompositions("JetpackCompose.app", "Profile Picture")
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background("#F0F5F9".color, CircleShape)
            .clickable { onClick(item, index) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$count",
            color = Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(30.dp)
                .height(30.dp)
                .wrapContentHeight()
        )
    }
}

@Composable
fun ProfilePicture(alpha: Float, onClick: (String, Int) -> Unit, item: String, index: Int) {
    LogCompositions("JetpackCompose.app", "Profile Picture")
    Column {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "Contact profile picture",
            alpha = alpha,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)                       // clip to the circle shape
                .border(2.dp, Blue, CircleShape)
                .clickable { onClick(item, index) }
        )
        Text(
            text = "Raghunandan kavi",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun rememberScrollContext(listState: LazyListState): ScrollContext {
    val scrollContext by remember {
        derivedStateOf {
            ScrollContext(
                firstVisibleItem = listState.firstVisibleItemIndex,
                offset = listState.firstVisibleItemScrollOffset
            )
        }
    }
    return scrollContext
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

data class ScrollContext(
    val firstVisibleItem: Int,
    val offset: Int,
)

@Preview(showBackground = false)
@Composable
fun ProfilePreview() {
    Image(
        painter = painterResource(R.drawable.ic_launcher_background),
        contentDescription = "Contact profile picture",
        alpha = 1f,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .border(5.dp, Blue, CircleShape)
            .clickable { }
    )
}

@Preview(showBackground = false)
@Composable
fun ImageWithCount() {
    val onClick: (String, Int) -> Unit = { _: String, _: Int ->
    }
    ComposeLearningTheme {
        ProfilePictureWithCount(onClick = onClick, "A", 0, 6)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeLearningTheme {
        Greeting("Android")
    }
}

class Ref(var value: Int)

// Note the inline function below which ensures that this function is essentially
// copied at the call site to ensure that its logging only recompositions from the
// original call site.
@Composable
fun LogCompositions(tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        val ref = remember { Ref(0) }
        SideEffect { ref.value++ }
        Log.d(tag, "Compositions: $msg ${ref.value}")
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TooltipOnLongClickExample(onClick: () -> Unit = {}) {
    // Commonly a Tooltip can be placed in a Box with a sibling
    // that will be used as the 'anchor' for positioning.
    Box(
        Modifier
            .height(300.dp)
            .width(300.dp)
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        val showTooltip = remember { mutableStateOf(false) }
        // Buttons and Surfaces don't support onLongClick out of the box,
        // so use a simple Box with combinedClickable
        Box(
            modifier = Modifier
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    onClickLabel = "Button action description",
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = { showTooltip.value = true },
                ),
        ) {
            Text("Click Me (will show tooltip on long click)")
        }

        Tooltip(showTooltip) {
            // Tooltip content goes here.
            Text("Tooltip Text!!")
        }
    }
}

val String.color
    get() = Color(android.graphics.Color.parseColor(this))