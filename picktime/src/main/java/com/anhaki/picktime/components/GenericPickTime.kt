/*
 * Copyright 2024 Anhaki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications Copyright (c) 2026 SquadPlay contributors.
 */

package com.anhaki.picktime.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle

/**
 * A high-level composable that displays multiple wheels horizontally with a focus indicator.
 *
 * @param selectedTextStyle Text style for selected items inside the wheels.
 * @param verticalSpace Vertical space around the selected item in the focus indicator.
 * @param containerColor Background color of the entire picker container.
 * @param focusIndicator The style configuration for the focus indicator.
 * @param content Content to be displayed.
 */

@Composable
internal fun GenericPickTime(
    selectedTextStyle: PickTimeTextStyle,
    verticalSpace: Dp,
    containerColor: Color,
    focusIndicator: PickTimeFocusIndicator,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var minContainerWidth by remember { mutableStateOf<Dp?>(null) }

    Box(
        modifier = Modifier
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        minContainerWidth?.let { width ->
            FocusIndicator(
                focusIndicator = focusIndicator,
                selectedTextStyle = selectedTextStyle,
                minWidth = width,
                verticalSpace = verticalSpace
            )
        }
        Row(
            modifier = Modifier
                .onGloballyPositioned {
                    minContainerWidth = with(density) { it.size.width.toDp() }
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}
