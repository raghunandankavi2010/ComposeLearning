package com.example.composelearning

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.composelearning.pager.CropBar
import com.example.composelearning.speedometer.Legend
import com.example.composelearning.speedometer.Speedometer3
import com.example.composelearning.ui.theme.ComposeLearningTheme
import com.example.composelearning.ui.theme.DetailsScreen
import com.example.composelearning.ui.theme.HomeScreen
import com.example.composelearning.ui.theme.Screen
import com.example.composelearning.ui.theme.ThirdScreen

class MainActivity : ComponentActivity() {
    @SuppressLint(
        "UnusedMaterialScaffoldPaddingParameter",
        "UnusedMaterial3ScaffoldPaddingParameter"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeLearningTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {

                    //JetpackComposeNavigationApp()
                    val mainViewModel: MainViewModel = viewModel()


                    TutorialNavGraph(mainViewModel)
//                    val shouldShowIcon by mainViewModel.searchWidgetVisibility.collectAsState()
//                    LogCompositions(tag = "Surface", msg = "${mainViewModel.hashCode()}")
//
//                    Scaffold(
//                        topBar = {
//                            DefaultAppBar(shouldShowIcon)
////                            TopAppBar(
////                                title = {
////                                    Text(text = "Compose Ui Samples")
////                                },
////                                navigationIcon = {
////                                    IconButton(onClick = { }) {
////                                        Icon(Icons.Filled.Menu, "")
////                                    }
////                                },
////                                backgroundColor = Blue,
////                                contentColor = White,
////                                elevation = 12.dp
////                            )
//                        }) {

//                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAppBar(
    shouldShowIcon: Boolean,
) {


    LogCompositions(tag = "AppBar", msg = shouldShowIcon.toString())

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TopAppBar(
            title = {
                Text(text = "Compose UI Sample",
                    modifier = Modifier
                        .clickable { }
                        // margin
                        .padding(start = 160.dp)
                )
            },
            actions = {
                if (shouldShowIcon) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.background(Color.Black),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Search Icon",
                            tint = Color.Black,
                            modifier = Modifier,
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun JetpackComposeNavigationApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(route = Screen.Home.route) {
            HomeScreen { name ->
                navController.navigate(Screen.Details.createRoute(name))
            }
        }
        composable(
            route = Screen.Details.route,
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            DetailsScreen(name = name, { navController.popBackStack() }, {
                navController.navigate(Screen.ThirdScreen.route)
            })
        }

        composable(
            route = Screen.ThirdScreen.route
        ) { backStackEntry ->

            ThirdScreen {
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun TutorialNavGraph(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "FirstScreen",
) {

    var redirect by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(redirect) {
        if (navController.currentBackStackEntry?.destination?.route != "FirstScreen") {
            navController.popBackStack(route = "FirstScreen", inclusive = false)
        }
    }

    val shouldRedirect: (Boolean) -> Unit = {
        redirect = !redirect
    }


    NavHost(
        modifier = modifier.statusBarsPadding(),
        navController = navController,
        startDestination = startDestination
    ) {


        composable(route = "FirstScreen") { navBackEntryStack ->
            //DraggableLineDrawing()
            //BoxAnim()
            Column(
                modifier = Modifier
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                CropBar() {

                }

//                val calendarViewModel: CalendarViewModel = viewModel()
//                CalendarScreen(onBackPressed = {
//                    navController.popBackStack()
//                }, mainViewModel = calendarViewModel)
//
//                PercentageBaseLayout()

//                MultiColorProgressCanvas(
//                    modifier = Modifier
//                        .height(16.dp)
//                        .padding(16.dp)
//                        .fillMaxSize(),
//                    heightOfProgress = 8.dp,
//                    cornerRadii = 2.dp
//                )
//
//                Spacer(modifier = Modifier.padding(top = 30.dp))
//
//                Column(
//                    modifier = Modifier
//                        .height(150.dp)
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                        .align(Alignment.CenterHorizontally)
//                ) {
//                    Row(
//                        modifier = Modifier.align(Alignment.CenterHorizontally),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Legend(Color(0xFFE30513), "Danger", alpha = true)
//
//                        Legend(Color(0xFFF7AB20), "Stress", alpha = true)
//
//                        Legend(Color(0xFF25AB21), "Optimum", alpha = true)
//
//                        Legend(Color(0xFF2253DA), "Excess", alpha = false)
//                    }
//
//                    Speedometer3(
//                        modifier = Modifier.padding(top = 16.dp),
//                        25,
//                        10,
//                        10,
//                        55,
//                        50
//                    )
//
//                    Row(
//                        modifier = Modifier
//                            .align(Alignment.CenterHorizontally)
//                            .padding(top = 16.dp)
//                            .height(32.dp)
//                    ) {
//
//                        Text(
//                            modifier = Modifier,
//                            text = "68.1",
//                            style = TextStyle(
//                                fontSize = 32.sp,
//                                lineHeight = 32.sp,
//                                fontFamily = FontFamily(Font(R.font.jio_type_black)),
//                                fontWeight = FontWeight(900),
//                                color = Color(0xFF141414),
//
//                                )
//                        )
//                        Box(
//                            modifier = Modifier
//                                .align(Alignment.Bottom)
//                                .width(18.dp)
//                                .height(24.dp)
//                                .padding(start = 2.dp, end = 1.dp)
//                        ) {
//                            Text(
//                                modifier = Modifier.padding(top = 3.dp),
//                                text = "%",
//                                style = TextStyle(
//                                    fontSize = 18.sp,
//                                    lineHeight = 24.sp,
//                                    fontFamily = FontFamily(Font(R.font.jio_type_light)),
//                                    fontWeight = FontWeight(700),
//                                    color = Color(0xFF141414),
//                                    textAlign = TextAlign.Center
//                                )
//                            )
//                        }
//                    }
//
//                    Text(
//                        modifier = Modifier
//                            .padding(top = 4.dp)
//                            .align(Alignment.CenterHorizontally),
//                        text = "Soil moisture (vwc%)",
//                        style = TextStyle(
//                            fontSize = 14.sp,
//                            lineHeight = 20.sp,
//                            fontFamily = FontFamily(Font(R.font.jio_type_medium)),
//                            fontWeight = FontWeight(500),
//                            color = Color(0xA6000000),
//
//                            textAlign = TextAlign.Center,
//                        )
//                    )
//
//                }
//
//                Spacer(modifier = Modifier.padding(top = 16.dp))
//
//                TemperatureChart3(
//                    modifier = Modifier
//                        .padding(start = 16.dp, end = 16.dp)
//                        .height(78.dp)
//                        .fillMaxWidth(), 20, -15, 60
//                )
//
//                Spacer(modifier = Modifier.padding(top = 16.dp))
//
//                TemperatureChart(
//                    modifier = Modifier
//                        .padding(start = 16.dp, end = 16.dp)
//                        .height(78.dp)
//                        .fillMaxWidth(), 30, 0, 60
//                )
//
//                Spacer(modifier = Modifier.padding(top = 16.dp))
//
//                Box(modifier = Modifier) {
//
//                    PieChartPreview { _, _ ->
//                    }
//                }
//
//                CreateAlertDialog()
                //ParentComposable()

                //ProgressMeter()
                //GeneralAlertsList()

//                val listViewModel: ListViewModel = viewModel()
//
//                DummyList(listViewModel) {
//                    listViewModel.updateItems(UIActions.Update(it))
//                }

//                val context = LocalContext.current
//                val list = remember { mutableStateListOf<CropHolder>() }
//                LaunchedEffect(Unit) {
//                    repeat(3) {
//                        val cropHolder = CropHolder(R.drawable.tomato, R.drawable.ic_remove, it)
//                        list.add(cropHolder)
//                    }
//                }
//                //ImageWithAction()
//                val cropList by remember { mutableStateOf(getCropList()) }
//                val selectedIds = remember { mutableStateOf(emptySet<Int>()) }
//                CropScreen(list,cropList, selectedIds, { selected, cropId ->
//                    if (!selectedIds.value.contains(cropId) && selectedIds.value.size + list.size > 9) {
//                        selectedIds.value = selectedIds.value.minus(cropId)
//                        Toast.makeText(
//                            context,
//                            "Cannot select more than 10 crops",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } else {
//                        selectedIds.value = if (selected) {
//                            selectedIds.value.plus(cropId)
//                        } else {
//                            selectedIds.value.minus(cropId)
//                        }
//                    }
//                }, { cropId, index ->
//                    if (list.isNotEmpty()) {
//                        Toast.makeText(
//                            context,
//                            "Item Deleted at $index",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        list.removeAt(index)
//                    }
//                })
//                ButtonSandbox()
//                CustomRoundedButton(modifier = Modifier.width(200.dp).padding(top = 20.dp), content = {
//                    CircularProgressIndicator(
//                        modifier = Modifier.wrapContentHeight(),
//                        color = MaterialTheme.colorScheme.onPrimary,
//                        trackColor = MaterialTheme.colorScheme.onPrimary, // just to make it completely visible in preview
//                    )
//                }) {
//
//                }
                //Pager2()

//                ThermometerCanvas(
//                    modifier = Modifier
//                        .size(300.dp)
//                        .background(Color.Black)
//                        .align(Alignment.CenterHorizontally)
//                )

                //PagerIndicatorDemo()

//                ButtonWithBorder(
//                    textColor = Color.Red,
//                    text = "Get Stareted",
//                    modifier = Modifier.height(40.dp).width(200.dp),
//                    backgroundColor = Color(0xFFBB86FC),
//
//                    ) {
//
//                }
                //ShapeTry()
                // Speedometer(100)
                //LazyRowLikePager()
                //navController.popBackStack("A",inclusive = true) in c
                //PagerIndicatorDemo()
                //CircleRowWithTextAndImage()

//                val focusRequester = remember { FocusRequester() }
//                val focusManager = LocalFocusManager.current
//                var text by remember {
//                    mutableStateOf("")
//                }
//
//                OTPTextField(
//                    value = text,
//                    length = 4,
//                    onValueChange = {
//                        text = it
//                        if (text.length == 4) {
//                            // Handle the case when the OTP is complete
//                            focusManager.clearFocus(true)
//                        }
//                    }
//                )
//
//                PagerDemo3()
//                ThermometerCanvas(
//                    modifier = Modifier
//                        .size(300.dp)
//                        .background(Color.Black)
//                        .align(Alignment.CenterHorizontally)
//                )
//                PieChartPreview() { data, index ->
//
//                }
//                PagerIndicatorDemo()
//                BoxAnim2() {
//                    navController.navigate("SecondScreen")
//                }
                //GeneralAlertsList()
//                ThermometerCanvas(
//                    modifier = Modifier
//                        .size(300.dp)
//                        .background(Color.Black)
//                        .align(Alignment.CenterHorizontally)
//                )
                //NumberPicker(Modifier.padding(top = 50.dp))
                // BoxOverlap(modifier =  Modifier.padding(top = 50.dp).align(Alignment.CenterHorizontally))
                //LazyRowLikePager()

//                PagerIndicatorDemo()
//                val progress = remember {
//                    50
//                }
                //GeneralAlertsList()
                //SwipetoDismiss()
//                PieChartPreview(){ chartData,index ->
//
//                }
            }
        }
        //SpeedometerScreen()
        //Speedometer2(progress)
        //Speedometer(progress)
//                Spacer(modifier = Modifier.padding(16.dp))
//                Text(
//                    text = "E",
//                    textAlign = TextAlign.Center,
//                    color = Color.Red,
//                    modifier = Modifier
//                        .padding(top = 50.dp)
//                        .defaultMinSize(32.dp)
//                        .background(Color.Black, shape = CircleShape)
//                        .circleLayout()
//
//                )
//                val focusRequester = remember { FocusRequester() }
//                val focusManager = LocalFocusManager.current
//                var text by remember {
//                    mutableStateOf("")
//                }
//
//
//                OTPTextField(
//                    value = text,
//                    length = 4,
//                    onValueChange = {
//                        text = it
//                        if (text.length == 4) {
//                            // Handle the case when the OTP is complete
//                            focusManager.clearFocus(true)
//                        }
//                    }
//                )
//
//                Spacer(modifier = Modifier.padding(16.dp))
//                BoxAnim()
//                Spacer(modifier = Modifier.padding(16.dp))
//                CircleRowWithTextAndImage()
//                Spacer(modifier = Modifier.padding(16.dp))
//                PagerIndicatorDemo()


        //PagerDemo3()
        //MaxWidthText()
//            Column(modifier = Modifier.height(80.dp).padding(top = 20.dp)) {
//                EquiRow()
//            }

        //AnotherProgressBar()
        //ContinuousLineGraph()
        //AnimatedHeartShape()
        //ImageWithRedDot()
//            Box {
//                                            Image(
//                                painter = painterResource(id = R.drawable.ic_launcher_background),
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .width(200.dp)
//                                    .height(200.dp)
//                                    .clip(Polygon(5,-90f)),
//                                contentScale = ContentScale.Crop
//                            )
//            }

        // Filters()
//            val items = remember {
//                listOf("Man", "Woman")
//            }
//
//            var selectedIndex by remember {
//                mutableStateOf(0)
//            }
//            Column(Modifier.background(Color.Green)) {
//
//
//                com.example.composelearning.buttons.TextSwitch(
//                    selectedIndex = selectedIndex,
//                    items = items,
//                    onSelectionChange = {
//                        selectedIndex = it
//                    }
//                )
        //Chart()
        // EquiRow()
        //LazyRowWithColorAnimation()
        //Speedometer(progress = 100)
//                InstagramCarousel(
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                        .fillMaxWidth()
//                        .padding(vertical = 16.dp),
//                    currentValueLabel = { value ->
//                        Text(
//                            text = "$value",
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                    })
        //           }

        //MarqueeText(LoremIpsum().values.first().take(90))
        composable("SecondScreen") {
            SecondScreen(shouldRedirect)
        }
    }

}


//                        Column(
//                            modifier = Modifier
//                                .fillMaxSize(),
//                            verticalArrangement = Arrangement.Top,
//                        ) {
//
//                            AlternateUI()
// CustomArcShape Composable
// CustomTopArcShapeComposable(Modifier.padding(horizontal = 32.dp))
// Infinite circular progress animation
//                            CircleProgressInfinite()
// Max width of two texts in a row composable
//                            MaxWidthText()
// Circular list
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

@Preview(showBackground = true)
@Composable
fun Telegu() {

    Text(
        modifier = Modifier,
        text = "స్ప్రే చేశారా?",
        style = TextStyle(
            fontSize = 32.sp,
            lineHeight = 32.sp,
            fontFamily = FontFamily(Font(R.font.jio_type_black)),
            fontWeight = FontWeight(900),
            color = Color(0xFF141414),

            )
    )
}

@Preview(widthDp = 300, showBackground = true)
@Composable
fun PreviewScreen() {

    Column {
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Legend(Color(0xFFE30513), "ప్రమాదం", alpha = true)

            Legend(Color(0xFFF7AB20), "ఒత్తిడి", alpha = false)

            Legend(Color(0xFF25AB21), "వాంఛనీయ", alpha = true)

            Legend(Color(0xFF2253DA), "అదనపు", alpha = true)
        }
        Speedometer3(
            modifier = Modifier.padding(top = 16.dp),
            25,
            10,
            10,
            55,
            50
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
                .height(32.dp)
        ) {

            Text(
                modifier = Modifier,
                text = "68.1",
                style = TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 32.sp,
                    fontFamily = FontFamily(Font(R.font.jio_type_black)),
                    fontWeight = FontWeight(900),
                    color = Color(0xFF141414),

                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .width(18.dp)
                    .height(24.dp)
                    .padding(start = 2.dp, end = 1.dp)
            ) {
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = "%",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.jio_type_light)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFF141414),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }

}