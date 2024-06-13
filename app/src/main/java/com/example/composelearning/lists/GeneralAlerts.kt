package com.example.composelearning.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.composelearning.R

data class AlertsData(val time: String, val type: String, val content: String, val id: Int) {

}


fun getAlertsData(): MutableList<AlertsData> {
    val list = mutableListOf<AlertsData>()
    repeat(40) {
        list.add(
            AlertsData(
                "9:00pm,12/08/2024",
                "Fertilizer Land. Keeping cotton field weed free for initial 60 days is very important. To avoid weed infestation,  spray the field with Pendimethalin 38.7% CS @  ml/liter of water. Spray the solution on entire field. Keeping cotton field weed free for initial 60 days is very important. To avoid weed infestation,  spray the field with Pendimethalin 38.7% CS @  ml/liter of water. Spray the solution on entire field",
                "Keeping cotton field weed free for initial 60 days is very important. To avoid weed infestation,  spray the field with Pendimethalin 38.7% CS @  ml/liter of water. Spray the solution on entire field. Keeping cotton field weed free for initial 60 days is very important. To avoid weed infestation,  spray the field with Pendimethalin 38.7% CS @  ml/liter of water. Spray the solution on entire field",
                it
            )
        )
    }
    return list
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun GeneralAlertsList() {
    val list = getAlertsData()
    val expand = remember { mutableStateMapOf<Int, Boolean>() }
    LazyColumn(
        modifier = Modifier
            .semantics {
                testTagsAsResourceId = true // typically at the root ui element
            }
            .background(Color(0x29000000))
            .padding(top = 16.dp)
    ) {

        items(list.size, key = { index -> list[index].id }) { index ->
            GeneralAlerts(list[index],expand, index)
            Spacer(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun GeneralAlerts(alertsData: AlertsData, expand: SnapshotStateMap<Int, Boolean>, index: Int) {

    ElevatedCard(
        modifier = Modifier
              .testTag(
                "Alerts${alertsData.id}"
                )
            .fillMaxSize()
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {

        val context = LocalContext.current
        val fontFamily = remember {
            FontFamily(
                typeface = ResourcesCompat.getFont(context, R.font.jio_type_medium)!!
            )
        }

        val fontFamilyBold = remember {
            FontFamily(
                typeface = ResourcesCompat.getFont(context, R.font.jio_type_bold)!!
            )
        }

        val fontFamilyNormal = remember {
            FontFamily(
                typeface = ResourcesCompat.getFont(context, R.font.jio_type_medium)!!
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, top = 24.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    maxLines = 1,
                    style = TextStyle(fontSize = 12.sp, fontFamily = fontFamily),
                    color = Color(0xFF777777),
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                        .weight(2f),
                    text = alertsData.time
                )


                val drawable =
                    if (expand[index] == true) {
                        R.drawable.icon_up
                    } else {
                        R.drawable.icon_down
                    }

                Image(
                    painter = painterResource(drawable),
                    contentDescription = "Arrow Down",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            val isExpand = expand[index] ?: false
                            expand[index] = !isExpand
                        }
                )
            }
            Text(
                modifier = Modifier
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = Color.Black,
                style = TextStyle(fontSize = 16.sp, fontFamily = fontFamilyBold),
                text = alertsData.type
            )

            val modifier =
                if (expand[index] == true) {
                    Modifier.wrapContentHeight()
                } else {
                    Spacer(modifier = Modifier.padding(bottom = 16.dp))
                    Modifier.height(0.dp)
                }

            AnimatedVisibility(
                visible = expand[index] ?: false,
            ) {

                Text(
                    modifier = modifier
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    color = Color(0xFF777777),
                    style = TextStyle(fontSize = 14.sp, fontFamily = fontFamilyNormal),
                    text = alertsData.content
                )

            }

        }

    }
}

