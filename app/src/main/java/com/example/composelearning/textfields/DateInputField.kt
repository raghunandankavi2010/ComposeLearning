package com.example.composelearning.textfields

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
//
//@Composable
//fun BasicTextFieldDateUI(
//    date: String,
//    item: CropConditionEntity,
//    onboardingState: MutableMap<String, CropValidationEntity?>,
//    datePickerClick: (CropConditionEntity) -> Unit,
//    onValueChange: (String) -> Unit
//) {
//
//    BasicTextField(
//        modifier = Modifier
//            .fillMaxWidth()
//            .jkTestTag(item.apiKey + "_text_field")
//            .clickable {
//                datePickerClick(item)
//            },
//        value = date,
//        readOnly = true,
//        onValueChange = { newValue ->
//            onValueChange(newValue)
//        },
//        decorationBox = { innerTextField ->
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(0.82f),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    var dateDisplay = listOf<String>()
//                    if (date != DD_MM_YYYY_DATE_DEFAULT) {
//                        dateDisplay =
//                            FarmUtils.splitDateInListDDMMYYYY(onboardingState[item.apiKey]?.answerValue.orEmpty())
//                    }
//                    if (date != DD_MM_YYYY_DATE_DEFAULT) {
//                        DisplayDateNewUi(DD_JK_TAG, text = dateDisplay[0]) {
//                            datePickerClick(item)
//                        }
//                    } else {
//                        DisplayDateNewUi(DD_JK_TAG, text = DD_JK_TAG) {
//                            datePickerClick(item)
//                        }
//                    }
//                    Image(
//                        painter = painterResource(id = R.drawable.vertical_date_divider),
//                        contentDescription = "vertical date divider"
//                    )
//                    if (date != DD_MM_YYYY_DATE_DEFAULT && dateDisplay.size >= 2) {
//                        DisplayDateNewUi(MM_JK_TAG, text = dateDisplay[1]) {
//                            datePickerClick(item)
//                        }
//                    } else {
//                        DisplayDateNewUi(MM_JK_TAG, text = MM_JK_TAG) {
//                            datePickerClick(item)
//                        }
//                    }
//                    Image(
//                        painter = painterResource(id = R.drawable.vertical_date_divider),
//                        contentDescription = "vertical date divider"
//                    )
//                    if (date != "dd-mm-yyyy" && dateDisplay.size == 3) {
//                        DisplayDateNewUi(YYYY_JK_TAG, text = dateDisplay[2]) {
//                            datePickerClick(item)
//                        }
//                    } else {
//                        DisplayDateNewUi(YYYY_JK_TAG, text = YYYY_JK_TAG) {
//                            datePickerClick(item)
//                        }
//                    }
//                }
//                Image(
//                    painter = painterResource(id = R.drawable.date_button_image),
//                    contentDescription = stringResource(id = R.string.image_description),
//                    modifier = Modifier
//                        .weight(0.2f)
//                        .padding(8.dp)
//                        .align(Alignment.CenterVertically)
//                        .jkTestTag(item.apiKey + "_date_image")
//                )
//            }
//        }
//    )
//}
