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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.PREVIEW_CALENDAR_UI
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveLabelByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import java.time.format.TextStyle
import kotlin.math.roundToInt

@Composable
fun Calendar(
    ui: CalendarUI,
    windowSize: WindowSizeClass,
    onPreviousMonthTap: (LocalDate) -> Unit,
    onNextMonthTap: (LocalDate) -> Unit,
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
                            onNextMonthTap(ui.yearMonth.plus(1, DateTimeUnit.MONTH))
                        } else if (offset > swipeThresholdPx) {
                            onPreviousMonthTap(ui.yearMonth.minus(1, DateTimeUnit.MONTH))
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
            color = DesignSystemTheme.colorScheme.outline,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(10.dp)
        )
    }
}

@Composable
fun Header(
    windowSize: WindowSizeClass,
    yearMonth: LocalDate,
    onPreviousMonthTap: (LocalDate) -> Unit,
    onNextMonthTap: (LocalDate) -> Unit,
) {
    Row {
        IconButton(onClick = { onPreviousMonthTap(yearMonth.minus(1, DateTimeUnit.MONTH)) }) {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_left_24),
                contentDescription = "previous",
                tint = DesignSystemTheme.colorScheme.onSurface
            )
        }
        // Localized month name using java.time.Month as helper
        val monthName = java.time.Month.valueOf(yearMonth.month.name).getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)
        Text(
            text = "$monthName ${yearMonth.year}",
            textAlign = TextAlign.Center,
            color = DesignSystemTheme.colorScheme.onSurface,
            style = adaptiveTitleByHeight(windowSize),
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        IconButton(onClick = { onNextMonthTap(yearMonth.plus(1, DateTimeUnit.MONTH)) }) {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_right_24),
                contentDescription = "next",
                tint = DesignSystemTheme.colorScheme.onSurface
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
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .clip(SquircleShape(cornerSmoothing = CornerSmoothing.Small))
                .background(color = if (date.isSelected) DesignSystemTheme.colorScheme.primary else Color.Transparent)
                .size(48.dp)
                .clickable(enabled = date.enabled) {
                    onItemTap(date)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth?.toString() ?: "",
                color = when {
                    !date.enabled -> DesignSystemTheme.colorScheme.outlineVariant
                    date.isSelected -> DesignSystemTheme.colorScheme.onPrimary
                    else -> DesignSystemTheme.colorScheme.onSurface
                },
                style = adaptiveBodyByHeight(windowSize),
            )

        }

        if (date.countEvents > 0) {
            Badge(
                modifier = Modifier.align(Alignment.TopEnd).offset(6.dp, (-6).dp),
                containerColor = when {
                    !date.enabled -> DesignSystemTheme.colorScheme.outlineVariant
                    date.isSelected -> DesignSystemTheme.colorScheme.onPrimary
                    else -> DesignSystemTheme.colorScheme.primary
                },
                contentColor = when {
                    !date.enabled -> DesignSystemTheme.colorScheme.surface
                    date.isSelected -> DesignSystemTheme.colorScheme.primary
                    else -> DesignSystemTheme.colorScheme.onPrimary
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    if (date.hasUserEvents) {
                        Text(
                            text = "★",
                            style = adaptiveLabelByHeight(windowSize),
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }
                    Text(
                        text = date.countEvents.toString(),
                        style = adaptiveLabelByHeight(windowSize)
                    )
                }
            }
        }
    }
}

@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun CalendarPreview() {
    DesignSystemTheme {
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
