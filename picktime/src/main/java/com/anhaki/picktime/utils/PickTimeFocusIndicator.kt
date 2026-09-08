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

package com.anhaki.picktime.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A configuration data class that defines how the focus indicator (highlighted area)
 * in a wheel picker should look and behave.
 *
 * @param enabled Whether the focus indicator is displayed or not.
 * @param widthFull Whether the focus indicator should stretch to full width.
 * @param background The background color of the focus indicator.
 * @param shape The shape of the focus indicator (e.g., rectangle, rounded, etc.).
 * @param border The border style (color and thickness) of the focus indicator.
 */

data class PickTimeFocusIndicator(
    val enabled: Boolean,
    val widthFull: Boolean = true,
    val background: Color = Color(0xFFFFFFFF),
    val shape: Shape = RectangleShape,
    val border: BorderStroke = BorderStroke(0.dp, Color(0xFFFFFFFF))
)