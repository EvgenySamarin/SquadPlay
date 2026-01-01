package com.eysamarin.squadplay.ui.calendar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.Date
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
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun Calendar(
    ui: CalendarUI,
    windowSize: WindowSizeClass,
    onPreviousMonthTap: (YearMonth) -> Unit,
    onNextMonthTap: (YearMonth) -> Unit,
    onDateTap: (Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val horizontalDragOffset = remember { Animatable(0f) }
    val swipeThresholdPx = with(LocalDensity.current) { 100.dp.toPx() }
    val dragLimitPx = swipeThresholdPx * 1.5f

    LaunchedEffect(ui.yearMonth) {
        horizontalDragOffset.snapTo(0f)
    }

    Column(
        modifier = modifier.pointerInput(ui.yearMonth) {
            detectHorizontalDragGestures(
                onHorizontalDrag = { _, dragAmount ->
                    coroutineScope.launch {
                        val newOffset = horizontalDragOffset.value + dragAmount
                        val clampedOffset = newOffset.coerceIn(-dragLimitPx, dragLimitPx)
                        horizontalDragOffset.snapTo(clampedOffset)
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        val offset = horizontalDragOffset.value
                        if (offset < -swipeThresholdPx) {
                            onNextMonthTap(ui.yearMonth.plusMonths(1))
                        } else if (offset > swipeThresholdPx) {
                            onPreviousMonthTap(ui.yearMonth.minusMonths(1))
                        } else {
                            horizontalDragOffset.animateTo(0f, animationSpec = tween(300))
                        }
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        horizontalDragOffset.animateTo(0f, animationSpec = tween(300))
                    }
                }
            )
        }
    ) {
        Row {
            repeat(ui.daysOfWeek.size) {
                val item = ui.daysOfWeek[it]
                WeekDayItem(day = item, modifier = Modifier.weight(1f), windowSize = windowSize)
            }
        }
        Column(
            modifier = Modifier.offset { IntOffset(horizontalDragOffset.value.roundToInt(), 0) }
        ) {
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
                painter = painterResource(R.drawable.ic_keyboard_arrow_left_24),
                contentDescription = "previous",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            style = adaptiveTitleByHeight(windowSize),
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        IconButton(onClick = { onNextMonthTap(yearMonth.plusMonths(1)) }) {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_right_24),
                contentDescription = "next",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CalendarContent(
    windowSize: WindowSizeClass,
    dates: List<Date>,
    onDateTap: (Date) -> Unit,
) {
    Column {
        var index = 0
        repeat(6) {
            if (index >= dates.size) return@repeat
            Row {
                repeat(7) {
                    val item = if (index < dates.size) dates[index] else Date.Empty
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
    date: Date,
    onItemTap: (Date) -> Unit,
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
