package com.eysamarin.squadplay.designSystem.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.VariantPreviewText

enum class ToggleState {
    ON,
    OFF,
}

@Composable
fun Toggle(
    modifier: Modifier = Modifier,
    state: ToggleState = ToggleState.OFF,
    enabled: Boolean = true,
    onStateChange: (ToggleState) -> Unit = {},
) {
    Switch(
        checked = state == ToggleState.ON,
        onCheckedChange = { isChecked ->
            val newState = if (isChecked) ToggleState.ON else ToggleState.OFF
            onStateChange(newState)
        },
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = DesignSystemTheme.colorScheme.onPrimary,
            checkedTrackColor = DesignSystemTheme.colorScheme.primary,
            uncheckedThumbColor = DesignSystemTheme.colorScheme.outline,
            uncheckedTrackColor = DesignSystemTheme.colorScheme.surfaceContainerHighest,
            uncheckedBorderColor = DesignSystemTheme.colorScheme.outline,
        ),
    )
}

@DarkLightModePreview
@Composable
private fun TogglePreview() {
    DesignSystemTheme {
        Column(
            modifier = Modifier.background(DesignSystemTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            VariantPreviewText("Variant Default:")
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Enabled")
                        Toggle(state = ToggleState.ON)
                        Toggle(state = ToggleState.OFF)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Disabled")
                        Toggle(state = ToggleState.ON, enabled = false)
                        Toggle(state = ToggleState.OFF, enabled = false)
                    }
                }
            }
        }
    }
}