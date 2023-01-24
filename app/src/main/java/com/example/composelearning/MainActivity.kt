package com.example.composelearning

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Space
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.customshapes.DrawCubic
import com.example.composelearning.customshapes.TicketComposable
import com.example.composelearning.lists.*
import com.example.composelearning.pager.PagerDemo
import com.example.composelearning.rows.MaxWidthText
import com.example.composelearning.sotry.SOTry
import com.example.composelearning.sotry.Tracks
import com.example.composelearning.sotry.getTracksLists
import com.example.composelearning.textfields.AmountTextField
import com.example.composelearning.ui.theme.ComposeLearningTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
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
                                    Text(text = "Compose Ui Samples")
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
                        }) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                        ) {

                            MaxWidthText()

//                            var currentContext = LocalContext.current
//                            CircularList(
//                                itemWidthDp = 50.dp,
//                                visibleItems = 5,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(50.dp)
//                                    .background(Color.Black),
//                                currentIndex = {
//                                    Toast.makeText(currentContext,"$it",Toast.LENGTH_LONG).show()
//                                }
//                            ) {
//                                for (i in 0 until 40) {
//                                    RowItem(
//                                        color = colors[i % colors.size],
//                                    )
//                                }
//                            }

//                            Tracks(tracks = getTracksLists())
//                           val colors = listOf(
//                                Color.Red,
//                                Color.Green,
//                                Color.Blue,
//                                Color.Magenta,
//                                Color.Yellow,
//                                Color.Cyan,
//                            )
//                            CircularList(
//                                visibleItems = 5,
//                                modifier = Modifier.fillMaxSize(),
//                            ) {
//                                for (i in 0 until 10) {
//                                    ListItem(
//                                        text = "Item #$i",
//                                        color = colors[i % colors.size],
//                                        modifier = Modifier.size(50.dp)
//                                    )
//                                }
//                            }
 //                           DrawCubic()
//                            AmountTextField(modifier = Modifier)
                          //  SingleSelectableItem(getList())
//
//                            CircularListVertical(
//                                visibleItems = 5,
//                                circularFraction = .65f,
//                                modifier = Modifier.fillMaxSize(),
//                            ) {
//                                for (i in 0 until 40) {
//                                    ListItem(
//                                        text = "Item #$i",
//                                        color = colors[i % colors.size],
//                                        modifier = Modifier.size(50.dp)
//                                    )
//                                }
//                            }
//                            TicketComposable(modifier = Modifier
//                                .height(200.dp))
//                            Image(
//                                painter = painterResource(id = R.drawable.ic_launcher_background),
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .width(200.dp)
//                                    .height(200.dp)
//                                    .clip(Polygon(5,-90f)),
//                                contentScale = ContentScale.Crop
//                            )
//
//                            InstagramCarousel(
//                                modifier = Modifier.align(Alignment.CenterHorizontally)
//                                    .fillMaxWidth()
//                                    .padding(vertical = 16.dp),
//                                currentValueLabel = { value ->
//                                  Text(
//                                        text = "$value",
//                                        style = MaterialTheme.typography.h6
//                                    )
//                                }
//                             )
                          //Spacer(modifier = Modifier.padding(16.dp))
                            //PagerDemo(modifier = Modifier)
//                            Spacer(modifier = Modifier.padding(16.dp))
//                            BoxWithConstraints(modifier = Modifier
//                                .align(Alignment.CenterHorizontally)
//                                .fillMaxWidth()
//                                .padding(top = 16.dp)) {
//                                CenterCircle(
//                                    modifier = Modifier.align(Alignment.Center),
//                                    fillColor = Color(android.graphics.Color.parseColor("#4DB6AC")),
//                                    strokeWidth = 5.dp
//                                )
//                                CircularList(
//                                    itemWidthDp = 50.dp,
//                                    visibleItems = 5,
//                                    currentIndex = {
//                                        Toast.makeText(this@MainActivity.applicationContext,"Current Index $it",Toast.LENGTH_LONG).show()
//                                    },
//                                    modifier = Modifier
//                                        .align(Alignment.Center)
//                                        .fillMaxWidth()
//                                        .height(50.dp),
//
//                                ) {
//                                    for (i in 0 until 40) {
//                                        RowItem(
//                                            modifier = Modifier,
//                                            color = colors[i % colors.size],
//                                        )
//                                    }
//                                }
//                            }

                            //CustomDropdownMenu()
                            //BottomPanel()
                            //Speedometer(progress = 100)
                            //DragGestureTest()
                            /* var progress by remember { mutableStateOf(0f) }
                             Spacer(modifier = Modifier.padding(16.dp))
                             Row {
                                 Text(text = "Test1")
                                 var range by remember { mutableStateOf(-20f..20f) }
                                 Row(
                                     modifier= Modifier.weight(1f),
                                 ){
                                     RangeSlider(
                                         values = range, onValueChange = {
                                             range = it
                                         },
                                         valueRange = -50f..50f
                                     )
                                 }
                                 Text(text = "Test2")
                             }
                             Spacer(modifier = Modifier.padding(16.dp))
                             Speedometer(50)
                             TicketComposable(modifier = Modifier
                                 .height(200.dp))
                                 Image(
                                     painter = painterResource(id = R.drawable.ic_launcher_background),
                                     contentDescription = null,
                                     modifier = Modifier
                                         .width(200.dp)
                                         .height(200.dp)
                                         .clip(Polygon(5,-90f)),
                                     contentScale = ContentScale.Crop
                                 )
                             Spacer(modifier = Modifier.padding(16.dp))

                             MultiColorProgressCanvas(
                                 modifier = Modifier
                                     .height(16.dp)
                                     .padding(16.dp)
                                     .fillMaxSize(),
                                 heightOfProgress = 8.dp,
                                 cornerRadii = 2.dp
                             )
                             Spacer(modifier = Modifier.padding(16.dp))


                             com.example.composelearning.sliders.Slider(
                                 onValueChangeFinished = {
                                     // do something on value change finished
                                     println(progress.toInt())
                                 },
                                 valueRange = 0f..100f,
                                 value = progress,
                                 onValueChange = { value ->
                                     progress = value
                                 },
                                 colors = SliderDefaults.colors(
                                     activeTrackColor = ActiveTrackColor,
                                     inactiveTrackColor = InactiveTrackColor
                                 )
                             )
                             Spacer(modifier = Modifier.padding(16.dp))
                             ButtonWithBorder(text = "Cancel", onClick = {
                             })

                             Spacer(modifier = Modifier.padding(16.dp))
                             FilledButton(text = "Checkin", onClick = {
                             })

                             Spacer(modifier = Modifier.padding(16.dp))
                             val customTextSelectionColors = TextSelectionColors(
                                 handleColor = DEFAULT060,
                                 backgroundColor = DEFAULT060.copy(alpha = 0.4f)
                             )
                             var text by remember { mutableStateOf("0%") }
                             CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                                 DottedUnderlineTextField(
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .padding(16.dp),
                                     value = text,
                                     singleLine = true,
                                     onValueChange = { newText ->
                                         text = newText
                                     },
                                     trailingIcon = {
                                         Icon(painter = painterResource(id = R.drawable.ic_edit),
                                             contentDescription = "Edit",
                                             tint = DEFAULT060)
                                     },
                                     colors = TextFieldDefaults.textFieldColors(
                                         backgroundColor = GREY094,
                                         textColor = DEFAULT020,
                                         focusedIndicatorColor = DEFAULT060,
                                         unfocusedIndicatorColor = DEFAULT060,
                                         leadingIconColor = DEFAULT060,
                                         cursorColor = DEFAULT060
                                     )
                                 )
                             }
                             Spacer(modifier = Modifier.padding(16.dp))
                             TintedIconButtonWithBorder(
                                 imageVector = Icons.Default.Add,
                                 modifier = Modifier.size(25.dp),
                                 onClick = {
                                 },
                                 borderColor = GRAY040,
                                 iconTintColor = GRAY040,
                                 strokeWidth = 2.dp,
                                 contentDescription = "Add"
                             )
                             Spacer(modifier = Modifier.padding(16.dp))
                             SquareProfileImage(
                                 modifier = Modifier
                                     .padding(16.dp)
                                     .size(20.dp),
                                 drawable = R.drawable.ic_launcher_background,
                                 radii = 5.dp
                             )
                             Spacer(modifier = Modifier.padding(16.dp))
                             MainContent()
                             Spacer(modifier = Modifier.padding(16.dp))
                             OverlappingRow(
                                 overlapFactor = 0.7f
                             ) {
                                 val images = intArrayOf(
                                     R.drawable.ic_launcher_background,
                                     R.drawable.ic_launcher_background,
                                     R.drawable.ic_launcher_background,
                                     R.drawable.ic_launcher_background,
                                     R.drawable.ic_launcher_background,
                                     R.drawable.ic_launcher_background
                                 )
                                 for (i in images.indices) {
                                     Image(
                                         painter = painterResource(id = images[i]),
                                         contentDescription = null,
                                         modifier = Modifier
                                             .width(30.dp)
                                             .height(30.dp)
                                             .border(width = 1.dp,
                                                 color = Color.White,
                                                 shape = CircleShape)
                                             .clip(CircleShape),
                                         contentScale = ContentScale.Crop
                                     )
                                 }
                                 Box(
                                     contentAlignment = Alignment.Center,
                                     modifier = Modifier
                                         .width(30.dp)
                                         .height(30.dp)
                                         .border(width = 1.dp,
                                             color = Color.Black,
                                             shape = CircleShape)
                                         .clip(CircleShape)
                                         .background(White),
                                 ) {
                                     Text(
                                         text = "10+",
                                         fontSize = 21.sp,
                                         fontWeight = FontWeight.Bold,
                                         color = Color.Black,
                                         textAlign = TextAlign.Center,
                                     )
                                 }
                             }*/
                        }
                    }
                }
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
    }
    ProfileList(list, currentPosition = currentPosition.value, onClick)
}

@Composable
fun ProfileList(list: List<String>, currentPosition: Int, onClick: (String, Int) -> Unit) {
    // Column(Modifier.padding(start = 8.dp, top = 20.dp, end = 8.dp)) {
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

    Row(
        modifier = Modifier.wrapContentWidth(),
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
        //  }
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
    count: Int,
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
            "+$count",
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
    count: Int,
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
        //RowItem(color = Color.Red)
        //BottomPanel()
        // Greeting("Android")
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
private val colors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Magenta,
    Color.Black,
    Color.Cyan,
    Color.DarkGray
)