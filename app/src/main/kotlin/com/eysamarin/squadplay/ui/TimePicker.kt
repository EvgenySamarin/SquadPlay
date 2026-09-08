package com.eysamarin.squadplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.models.PickerTimeUnit
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import java.util.Calendar

@Composable
fun TimePicker(
    title: String,
    windowSize: WindowSizeClass,
    onTimeChange: (PickerTimeUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTime = Calendar.getInstance()
    val currentHour = currentTime[Calendar.HOUR_OF_DAY]
    val currentMinute = currentTime[Calendar.MINUTE]

    var hour by remember { mutableIntStateOf(currentHour) }
    var minute by remember { mutableIntStateOf(currentMinute) }

    LaunchedEffect(hour, minute) {
        onTimeChange(PickerTimeUnit(hour, minute))
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            style = adaptiveBodyByHeight(windowSize),
            color = DesignSystemTheme.colorScheme.outline,
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DesignSystemTheme.colorScheme.surfaceContainer,
            ),
        ) {
            PickHourMinute(
                modifier = Modifier.padding(horizontal = 12.dp),
                initialHour = currentHour,
                onHourChange = { hour = it },
                initialMinute = currentMinute,
                onMinuteChange = { minute = it },
                containerColor = DesignSystemTheme.colorScheme.surfaceContainer,
                unselectedTextStyle = with(DesignSystemTheme.typography.bodyLarge) {
                    PickTimeTextStyle(
                        color = DesignSystemTheme.colorScheme.outline,
                        fontSize = fontSize,
                        fontFamily = fontFamily ?: FontFamily.Default,
                        fontWeight = fontWeight ?: FontWeight.Normal,
                    )
                },
                selectedTextStyle = with(DesignSystemTheme.typography.headlineLarge) {
                    PickTimeTextStyle(
                        color = DesignSystemTheme.colorScheme.onPrimaryContainer,
                        fontSize = fontSize,
                        fontFamily = fontFamily ?: FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                    )
                },
                focusIndicator = PickTimeFocusIndicator(
                    enabled = true,
                    widthFull = false,
                    shape = RoundedCornerShape(20.dp),
                    background = DesignSystemTheme.colorScheme.primaryContainer,
                )
            )
        }
    }
}

@DarkLightModePreview
@Composable
private fun TimePickerPreview() {
    DesignSystemTheme {
        TimePicker(
            title = "From",
            windowSize = WINDOWS_SIZE_MEDIUM,
            onTimeChange = { _ -> },
        )
    }
}
