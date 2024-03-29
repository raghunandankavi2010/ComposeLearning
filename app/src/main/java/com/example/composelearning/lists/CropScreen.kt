package com.example.composelearning.lists

import android.widget.Button
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R
import com.example.composelearning.images.CropImage
import androidx.compose.material3.Button


data class CropHolder(val cropImage: Int, val actionIcon: Int, val cropId: Int)

@Composable
fun CropScreen() {
    val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        ) {

            val list = remember { mutableStateListOf<CropHolder>() }
            LaunchedEffect(Unit) {
                repeat(3) {
                    val cropHolder = CropHolder(R.drawable.tomato, R.drawable.ic_remove, it)
                    list.add(cropHolder)
                }
            }

            val (modifier, textModifier) = if (list.isEmpty()) {
                Modifier.height(0.dp) to Modifier
                    .padding(top = 48.dp)
                    .wrapContentHeight()
                    .fillMaxWidth()
            } else {
                Modifier.wrapContentHeight() to Modifier.size(0.dp)
            }
            Text(
                modifier = Modifier
                    .animateContentSize()
                    .then(modifier),
                text = "My Crops", style = TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 32.sp,
                    fontFamily = FontFamily(Font(R.font.jio_type_black)),
                    fontWeight = FontWeight(900),
                    color = Color(0xFF141414),
                )
            )
            Spacer(
                modifier = Modifier
                    .animateContentSize()
                    .then(modifier)
                    .padding(top = 24.dp)
            )

            val mSelectedIds = remember { mutableStateOf(emptySet<Int>()) }
            LazyRow(
                modifier = Modifier
                    .animateContentSize()
                    .then(modifier),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(list) { index, item ->
                    val selected = mSelectedIds.value.contains(item.cropId)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CropImage(
                            currentIndex = index,
                            isRemoveIconShow = true,
                            cropId = item.cropId,
                            selected = selected,
                            cropImage = item.cropImage,
                            onClick = null,
                            onRemove = { cropId, index ->
                                if (list.isNotEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Item Deleted at $index",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    list.removeAt(index)
                                }
                            })
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "Tomato", style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontFamily = FontFamily(Font(R.font.jio_type_black)),
                                fontWeight = FontWeight(700),
                                color = Color(0xFF03753C),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                }
            }


            Text(
                modifier = textModifier
                    .animateContentSize(),
                text = "Select your crop.",
                style = TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 32.sp,
                    fontFamily = FontFamily(Font(R.font.jio_type_black)),
                    fontWeight = FontWeight(900),
                    color = Color(0xFF141414),
                    textAlign = TextAlign.Start
                )
            )

            val topPadding = if (list.isEmpty()) {
                16.dp
            } else {
                24.dp
            }
            Spacer(modifier = Modifier.padding(top = topPadding))
            Text(
                text = "Select up to 10 crops you are interested in.",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.jio_type_black)),
                    fontWeight = FontWeight(500),
                    color = Color(0xA6000000),
                )
            )

            Spacer(modifier = Modifier.padding(top = 32.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {

                val cropList by remember { mutableStateOf(getCropList()) }

                val selectedIds = remember { mutableStateOf(emptySet<Int>()) }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 66.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(cropList) { _, item ->
                        val selected = selectedIds.value.contains(item.cropId) // N
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CropImage(
                                isRemoveIconShow = false,
                                cropId = item.cropId,
                                selected = selected,
                                cropImage = item.cropImage,
                                onClick = { selected, cropId ->
                                    if (!selectedIds.value.contains(cropId) && selectedIds.value.size + list.size > 9) {
                                        selectedIds.value.minus(cropId)
                                        Toast.makeText(
                                            context,
                                            "Cannot select more than 10 crops",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        selectedIds.value = if (selected) {
                                            selectedIds.value.plus(cropId)
                                        } else {
                                            selectedIds.value.minus(cropId)
                                        }
                                    }
                                }, onRemove = null
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "Tomato", style = TextStyle(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = FontFamily(Font(R.font.jio_type_black)),
                                    fontWeight = FontWeight(700),
                                    color = Color(0xFF03753C),
                                    textAlign = TextAlign.Center,
                                )
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(top = 59.dp)) {
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .heightIn(48.dp)
                        .background(
                            color = Color(0xFF03753C),
                            shape = RoundedCornerShape(size = 250.dp)
                        )
                        .fillMaxWidth(),
                    onClick = {

                    }) {

                    Text(
                        text = "Save",
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily(Font(R.font.jio_type_medium)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF),

                            )
                    )
                }

            }
        }
    }


fun getCropList(): List<CropHolder> {
    val list = mutableListOf<CropHolder>()
    repeat(20) {
        val cropHolder = CropHolder(R.drawable.tomato, R.drawable.ic_select, it)
        list.add(cropHolder)
    }
    return list
}