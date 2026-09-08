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

enum class PickDateField {
    DAY, MONTH, YEAR
}

enum class PickDateOrder(val order: List<PickDateField>) {
    DMY(listOf(PickDateField.DAY, PickDateField.MONTH, PickDateField.YEAR)),
    MDY(listOf(PickDateField.MONTH, PickDateField.DAY, PickDateField.YEAR)),
    YMD(listOf(PickDateField.YEAR, PickDateField.MONTH, PickDateField.DAY)),
    YDM(listOf(PickDateField.YEAR, PickDateField.DAY, PickDateField.MONTH)),
    MYD(listOf(PickDateField.MONTH, PickDateField.YEAR, PickDateField.DAY)),
    DYM(listOf(PickDateField.DAY, PickDateField.YEAR, PickDateField.MONTH)),
}
