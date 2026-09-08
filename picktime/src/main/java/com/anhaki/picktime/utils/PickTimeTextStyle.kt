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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A custom text style holder used across pickers for consistent styling of wheel items.
 *
 * @param color The color of the text.
 * @param fontSize The size of the text.
 * @param fontFamily The font family used.
 * @param fontWeight The weight (thickness) of the font.
 */

data class PickTimeTextStyle(
    val color: Color = Color(0xFF404040),
    val fontSize: TextUnit = 24.sp,
    val fontFamily: FontFamily = FontFamily.Default,
    val fontWeight: FontWeight = FontWeight.Normal
) {
    /**
     * Converts this custom PickTimeTextStyle into a standard Compose TextStyle object
     * to be used in text composables.
     *
     * @return A TextStyle object matching the properties of PickTimeTextStyle.
     */
    fun toTextStyle(): TextStyle {
        return TextStyle(
            color = color,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight
        )
    }
}

