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

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.rememberTextMeasurer

/**
 * Measures the width of a specific text string using the given text style.
 * Useful for determining the width of a wheel picker item or focus indicator.
 *
 * @param text The string text to measure.
 * @param textStyle The custom text style applied to the text.
 * @return The width of the measured text as a Float.
 */

@Composable
internal fun measureTextWidth(
    text: String,
    textStyle: PickTimeTextStyle,
): Float {
    val textMeasurer = rememberTextMeasurer()

    val layoutResult = textMeasurer.measure(
        text = "$text ",
        style = textStyle.toTextStyle()
    )
    return layoutResult.size.width.toFloat()
}
