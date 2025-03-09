package com.eysamarin.squadplay.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.ui.theme.PrimaryFont
import com.eysamarin.squadplay.ui.theme.getAdaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.getAdaptiveLabelByHeight
import com.eysamarin.squadplay.ui.theme.getAdaptiveTitleByHeight
import java.time.YearMonth

@Composable
fun Calendar(
    ui: CalendarUI,
    windowSize: WindowSizeClass,
    onPreviousMonthTap: (YearMonth) -> Unit,
    onNextMonthTap: (YearMonth) -> Unit,
    onDateTap: (CalendarUI.Date) -> Unit,
) {
    Column {
        Row {
            repeat(ui.daysOfWeek.size) {
                val item = ui.daysOfWeek[it]
                WeekDayItem(day = item, modifier = Modifier.weight(1f), windowSize = windowSize)
            }
        }
        Header(
            windowSize = windowSize,
            yearMonth = ui.yearMonth,
            onPreviousMonthTap = onPreviousMonthTap,
            onNextMonthTap = onNextMonthTap
        )
        CalendarContent(
            windowSize = windowSize,
            dates = ui.dates,
            onDateTap = onDateTap
        )
    }
}

@Composable
fun WeekDayItem(day: String, modifier: Modifier = Modifier, windowSize: WindowSizeClass) {
    Box(modifier = modifier) {
        Text(
            text = day,
            style = getAdaptiveLabelByHeight(windowSize),
            color = PrimaryFont,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(10.dp)
        )
    }
}

@Composable
fun Header(
    windowSize: WindowSizeClass,
    yearMonth: YearMonth,
    onPreviousMonthTap: (YearMonth) -> Unit,
    onNextMonthTap: (YearMonth) -> Unit,
) {
    Row {
        IconButton(onClick = { onPreviousMonthTap(yearMonth.minusMonths(1)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "previous"
            )
        }
        Text(
            text = yearMonth.toString(),
            textAlign = TextAlign.Center,
            color = PrimaryFont,
            style = getAdaptiveTitleByHeight(windowSize),
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        IconButton(onClick = { onNextMonthTap(yearMonth.plusMonths(1)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "next"
            )
        }
    }
}

@Composable
fun CalendarContent(
    windowSize: WindowSizeClass,
    dates: List<CalendarUI.Date>,
    onDateTap: (CalendarUI.Date) -> Unit,
) {
    Column {
        var index = 0
        repeat(6) {
            if (index >= dates.size) return@repeat
            Row {
                repeat(7) {
                    val item = if (index < dates.size) dates[index] else CalendarUI.Date.Empty
                    ContentItem(
                        windowSize = windowSize,
                        date = item,
                        onItemTap = onDateTap,
                        modifier = Modifier.weight(1f)
                    )
                    index++
                }
            }
        }
    }
}

@Composable
fun ContentItem(
    windowSize: WindowSizeClass,
    date: CalendarUI.Date,
    onItemTap: (CalendarUI.Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = if (date.isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                }
            )
            .clickable {
                onItemTap(date)
            }
    ) {
        Text(
            text = date.dayOfMonth,
            color = PrimaryFont,
            style = getAdaptiveBodyByHeight(windowSize),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(10.dp)
        )
    }
}