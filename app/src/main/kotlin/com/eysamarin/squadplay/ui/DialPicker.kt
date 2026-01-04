
package com.eysamarin.squadplay.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.eysamarin.squadplay.models.DialPickerTarget
import com.eysamarin.squadplay.models.PickerTimeUnit
import com.eysamarin.squadplay.utils.DarkLightModePreview
import java.util.Calendar

@Composable
fun DialPicker(
    target: DialPickerTarget,
    onTimeChange: (PickerTimeUnit, DialPickerTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTime = Calendar.getInstance()
    val currentHour = currentTime[Calendar.HOUR_OF_DAY]
    val currentMinute = currentTime[Calendar.MINUTE]

    var hour by remember { mutableIntStateOf(currentHour) }
    var minute by remember { mutableIntStateOf(currentMinute) }


    LaunchedEffect(hour, minute) {
        onTimeChange(PickerTimeUnit(hour, minute), target)
    }

    Box(modifier = modifier) {
        PickHourMinute(
            initialHour = currentHour,
            onHourChange = { hour = it },
            initialMinute = currentMinute,
            onMinuteChange = { minute = it },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            unselectedTextStyle = with(MaterialTheme.typography.bodyLarge) {
                PickTimeTextStyle(
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = fontSize,
                    fontFamily = fontFamily ?: FontFamily.Default,
                    fontWeight = fontWeight ?: FontWeight.Normal,
                )
            },
            selectedTextStyle = with(MaterialTheme.typography.headlineLarge) {
                PickTimeTextStyle(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = fontSize,
                    fontFamily = fontFamily ?: FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                )
            },
            focusIndicator = PickTimeFocusIndicator(
                enabled = true,
                widthFull = false,
                shape = RoundedCornerShape(20.dp),
                background = MaterialTheme.colorScheme.primaryContainer,
            )
        )
    }
}

@DarkLightModePreview
@Composable
private fun DialPickerPreview() {
    DialPicker(
        target = DialPickerTarget.FROM,
        onTimeChange = { _, _ -> },
    )
}