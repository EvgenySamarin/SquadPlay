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
 * Measures the height of a text using the given text style.
 * This is useful for calculating the approximate height needed for a wheel picker item.
 *
 * @param textStyle The custom text style used for measuring.
 * @return The estimated text height as a Float, scaled by 0.6 for visual adjustment.
 */

@Composable
internal fun measureTextHeight(
    textStyle: PickTimeTextStyle,
): Float {
    val textMeasurer = rememberTextMeasurer()

    val layoutResult = textMeasurer.measure(
        text = " \n ",
        style = textStyle.toTextStyle()
    )

    return (layoutResult.size.height * 0.6).toFloat()
}
