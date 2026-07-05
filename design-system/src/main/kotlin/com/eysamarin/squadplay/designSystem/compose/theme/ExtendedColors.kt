package com.eysamarin.squadplay.designSystem.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

//region design system solid colors
val systemRedLight = Color(0xFFFF383C)
val systemRedDark = Color(0xFFFF4245)
val systemOrangeLight = Color(0xFFFF8D28)
val systemOrangeDark = Color(0xFFFF9230)
val systemYellowLight = Color(0xFFFFCC00)
val systemYellowDark = Color(0xFFFFD600)
val systemGreenLight = Color(0xFF34C759)
val systemGreenDark = Color(0xFF30D158)
val systemMintLight = Color(0xFF00C8B3)
val systemMintDark = Color(0xFF00DAC3)
val systemTealLight = Color(0xFF00C3D0)
val systemTealDark = Color(0xFF00D2E0)
val systemCyanLight = Color(0xFF00C0E8)
val systemCyanDark = Color(0xFF3CD3FE)
val systemBlueLight = Color(0xFF0088FF)
val systemBlueDark = Color(0xFF0091FF)
val systemIndigoLight = Color(0xFF6155F5)
val systemIndigoDark = Color(0xFF6B5DFF)
val systemPurpleLight = Color(0xFFCB30E0)
val systemPurpleDark = Color(0xFFDB34F2)
val systemPinkLight = Color(0xFFFF2D55)
val systemPinkDark = Color(0xFFFF375F)
val systemBrownLight = Color(0xFFAC7F5E)
val systemBrownDark = Color(0xFFB78A66)
//endregion

/**
 * This color palette comes from iOS system palette,
 * we are using this as reference for custom design tokens
 *
 * See more for link below
 * @see <a href="https://developer.android.com/develop/ui/compose/designsystems/custom#extending-material">
 *     Extending Material Theme </a>
 */
@Immutable
data class ExtendedColors(
    val red: Color,
    val orange: Color,
    val yellow: Color,
    val green: Color,
    val mint: Color,
    val teal: Color,
    val cyan: Color,
    val blue: Color,
    val indigo: Color,
    val purple: Color,
    val pink: Color,
    val brown: Color,
)

val LightExtendedColors = ExtendedColors(
    red = systemRedLight,
    orange = systemOrangeLight,
    yellow = systemYellowLight,
    green = systemGreenLight,
    mint = systemMintLight,
    teal = systemTealLight,
    cyan = systemCyanLight,
    blue = systemBlueLight,
    indigo = systemIndigoLight,
    purple = systemPurpleLight,
    pink = systemPinkLight,
    brown = systemBrownLight,
)

val DarkExtendedColors = ExtendedColors(
    red = systemRedDark,
    orange = systemOrangeDark,
    yellow = systemYellowDark,
    green = systemGreenDark,
    mint = systemMintDark,
    teal = systemTealDark,
    cyan = systemCyanDark,
    blue = systemBlueDark,
    indigo = systemIndigoDark,
    purple = systemPurpleDark,
    pink = systemPinkDark,
    brown = systemBrownDark,
)

/**
 * Provide custom colors in the Compose render tree
 */
val LocalExtendedColors = staticCompositionLocalOf(
    defaultFactory = {
        ExtendedColors(
            red = Color.Unspecified,
            orange = Color.Unspecified,
            yellow = Color.Unspecified,
            green = Color.Unspecified,
            mint = Color.Unspecified,
            teal = Color.Unspecified,
            cyan = Color.Unspecified,
            blue = Color.Unspecified,
            indigo = Color.Unspecified,
            purple = Color.Unspecified,
            pink = Color.Unspecified,
            brown = Color.Unspecified,
        )
    }
)