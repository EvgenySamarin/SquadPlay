package com.eysamarin.squadplay.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.PREVIEW_CALENDAR_UI
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveLabelByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import java.time.YearMonth

@Composable
fun Calendar(
    ui: CalendarUI,
    windowSize: WindowSizeClass,
    onPreviousMonthTap: (YearMonth) -> Unit,
    onNextMonthTap: (YearMonth) -> Unit,
    onDateTap: (CalendarUI.Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
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
            style = adaptiveLabelByHeight(windowSize),
            color = MaterialTheme.colorScheme.outline,
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
            color = MaterialTheme.colorScheme.onSurface,
            style = adaptiveTitleByHeight(windowSize),
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
            .clip(SquircleShape(cornerSmoothing = CornerSmoothing.Small))
            .background(color = if (date.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(enabled = date.enabled) {
                onItemTap(date)
            }
    ) {
        BadgedBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 12.dp),
            badge = {
                if (date.countEvents > 0) {
                    Badge(
                        modifier = Modifier.offset(x = 10.dp, y = (-8).dp),
                        containerColor = when {
                            !date.enabled -> MaterialTheme.colorScheme.outlineVariant
                            date.isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = when {
                            !date.enabled -> MaterialTheme.colorScheme.surface
                            date.isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onPrimary
                        }

                    ) {
                        Text(
                            text = date.countEvents.toString(),
                            style = adaptiveLabelByHeight(windowSize)
                        )
                    }
                }
            },
        ) {
            
            Text(
                modifier = Modifier,
                text = date.dayOfMonth?.toString() ?: "",
                color = when {
                    !date.enabled -> MaterialTheme.colorScheme.outlineVariant
                    date.isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = adaptiveBodyByHeight(windowSize),
            )
        }
    }
}

@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun CalendarPreview() {
    SquadPlayTheme {
        Column {
            Spacer(Modifier.padding(top = 24.dp))
            Calendar(
                ui = PREVIEW_CALENDAR_UI,
                windowSize = WINDOWS_SIZE_MEDIUM,
                onPreviousMonthTap = { },
                onNextMonthTap = { },
                onDateTap = { }
            )
        }
    }
}