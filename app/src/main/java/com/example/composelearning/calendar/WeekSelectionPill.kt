/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.composelearning.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.composelearning.calendar.model.CalendarState
import com.example.composelearning.calendar.model.CalendarUiState
import com.example.composelearning.calendar.model.Week
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekSelectionPill(
    week: Week,
    currentWeekStart: LocalDate,
    state: CalendarUiState,
    selectedPercentageTotalProvider: () -> Float,
    modifier: Modifier = Modifier,
    widthPerDay: Dp = 48.dp,
    pillColor: Color = Color(0xFFE5F7EE)
) {
    val widthPerDayPx = with(LocalDensity.current) { widthPerDay.toPx() }
    val cornerRadiusPx = with(LocalDensity.current) { 24.dp.toPx() }

    Canvas(
        modifier = modifier
            .padding(horizontal = 36.dp)
            .fillMaxWidth(),
        onDraw = {

            val (offset, size) = getOffsetAndSize(
                this.size.width,
                state,
                currentWeekStart,
                week,
                widthPerDayPx,
                cornerRadiusPx,
                selectedPercentageTotalProvider()
            )

            val translationX = if (state.animateDirection?.isBackwards() == true) -size else 0f

            translate(translationX) {

                drawRoundRect(
                    color = pillColor,
                    topLeft = offset,
                    size = Size(size, widthPerDayPx),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            }

        }
    )
}

/**
 * Calculates the animated Offset and Size of the red selection pill based on the CalendarState and
 * the Week SelectionState, based on the overall selectedPercentage.
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun getOffsetAndSize(
    width: Float,
    state: CalendarUiState,
    currentWeekStart: LocalDate,
    week: Week,
    widthPerDayPx: Float,
    cornerRadiusPx: Float,
    selectedPercentage: Float
): Pair<Offset, Float> {
    val numberDaysSelected = state.getNumberSelectedDaysInWeek(currentWeekStart, week.yearMonth)
    val monthOverlapDelay = state.monthOverlapSelectionDelay(currentWeekStart, week)
    val dayDelay = state.dayDelay(currentWeekStart)
    val edgePadding = (width - widthPerDayPx * CalendarState.DAYS_IN_WEEK) / 2

    val percentagePerDay = 1f / state.numberSelectedDays
    val startPercentage = (dayDelay + monthOverlapDelay) * percentagePerDay
    val endPercentage = startPercentage + numberDaysSelected * percentagePerDay

    val scaledPercentage = if (selectedPercentage >= endPercentage) {
        1f
    } else if (selectedPercentage < startPercentage) {
        0f
    } else {
        // Scale the overall percentage between the start selection of days to the end selection for
        // the current week. eg: if this week has 3 days before it selected, we only want to
        // start this animation after 3 * percentagePerDay and end it at the number of selected days
        // in the week - so we normalize the percentage between the startPercentage + endPercentage
        // to a range between at min 0f and 1f.
        normalize(
            selectedPercentage,
            startPercentage, endPercentage
        )
    }

    val scaledSelectedNumberDays = scaledPercentage * numberDaysSelected

    val sideSize = edgePadding + cornerRadiusPx

    val leftSize =
        if (state.isLeftHighlighted(currentWeekStart, week.yearMonth)) sideSize else 0f
    val rightSize =
        if (state.isRightHighlighted(currentWeekStart, week.yearMonth)) sideSize else 0f

    var totalSize = (scaledSelectedNumberDays * widthPerDayPx) +
            (leftSize + rightSize) * scaledPercentage
    if (dayDelay + monthOverlapDelay == 0 && numberDaysSelected >= 1) {
        totalSize = totalSize.coerceAtLeast(widthPerDayPx)
    }

    val startOffset =
        state.selectedStartOffset(currentWeekStart, week.yearMonth) * widthPerDayPx

    val offset =
        if (state.animateDirection?.isBackwards() == true) {
            Offset(startOffset + edgePadding + rightSize, 0f)
        } else {
            Offset(startOffset + edgePadding - leftSize, 0f)
        }

    return offset to totalSize
}

internal const val DURATION_MILLIS_PER_DAY = 150

private fun normalize(
    x: Float,
    inMin: Float,
    inMax: Float,
    outMin: Float = 0f,
    outMax: Float = 1f
): Float {
    val outRange = outMax - outMin
    val inRange = inMax - inMin
    return (x - inMin) * outRange / inRange + outMin
}


private fun Canvas.drawStartAndEndRoundedRectangles(
    offset: Offset,
    size: Float,
    cornerRadiusPx: Float,
    widthPerDayPx: Float,
    pillColor: Color
) {

}