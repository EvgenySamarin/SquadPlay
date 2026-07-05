package com.eysamarin.squadplay.designSystem.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * This typography comes from iOS typography system,  we are using this as reference for custom
 * design tokens until android specific design system will be implemented.
 *
 * There is agreements that any native implementation should use default system font.
 * So for android it will be roboto. Weight and letter-spacing will be taken according M3 guidelines.
 *
 *
 * [Figma typography link](link here)
 *
 * @see <a href="https://developer.android.com/develop/ui/compose/designsystems/custom#extending-material">
 *     Extending Material Theme </a>
 */
@Immutable
data class ExtendedTypography(
    val largeTitleRegular: TextStyle,
    val largeTitleEmphasized: TextStyle,
    val title1Regular: TextStyle,
    val title1Emphasized: TextStyle,
    val title2Regular: TextStyle,
    val title2Emphasized: TextStyle,
    val title3Regular: TextStyle,
    val title3Emphasized: TextStyle,
    val headline: TextStyle,
    val bodyRegular: TextStyle,
    val bodyEmphasized: TextStyle,
    val calloutRegular: TextStyle,
    val calloutEmphasized: TextStyle,
    val subheadlineRegular: TextStyle,
    val subheadlineEmphasized: TextStyle,
    val footnoteRegular: TextStyle,
    val footnoteEmphasized: TextStyle,
    val caption1Regular: TextStyle,
    val caption1Emphasized: TextStyle,
    val caption2Regular: TextStyle,
    val caption2Emphasized: TextStyle,
)

val ExtendedTypographyImpl = ExtendedTypography(
    largeTitleRegular = Typography.displaySmall,
    largeTitleEmphasized = Typography.displaySmall.copy(
        fontWeight = FontWeight.Bold,
    ),
    title1Regular = Typography.headlineMedium,
    title1Emphasized = Typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
    ),
    title2Regular = Typography.headlineSmall,
    title2Emphasized = Typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold,
    ),
    title3Regular = Typography.titleLarge,
    title3Emphasized = Typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    headline = Typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    bodyRegular = Typography.titleMedium.copy(
        fontWeight = FontWeight.Normal,
    ),
    bodyEmphasized = Typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    calloutRegular = Typography.titleMedium.copy(
        fontWeight = FontWeight.Normal,
    ),
    calloutEmphasized = Typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    subheadlineRegular = Typography.titleSmall.copy(
        fontWeight = FontWeight.Normal,
    ),
    subheadlineEmphasized = Typography.titleSmall.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    footnoteRegular = Typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
    ),
    footnoteEmphasized = Typography.bodyMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    caption1Regular = Typography.bodySmall,
    caption1Emphasized = Typography.bodySmall.copy(
        fontWeight = FontWeight.Medium,
    ),
    caption2Regular = Typography.labelSmall.copy(
        fontWeight = FontWeight.Normal,
    ),
    caption2Emphasized = Typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
    ),
)


/**
 * Provide custom colors in the Compose render tree
 */
val LocalExtendedTypography = staticCompositionLocalOf(
    defaultFactory = {
        ExtendedTypography(
            largeTitleRegular = TextStyle.Default,
            largeTitleEmphasized = TextStyle.Default,
            title1Regular = TextStyle.Default,
            title1Emphasized = TextStyle.Default,
            title2Regular = TextStyle.Default,
            title2Emphasized = TextStyle.Default,
            title3Regular = TextStyle.Default,
            title3Emphasized = TextStyle.Default,
            headline = TextStyle.Default,
            bodyRegular = TextStyle.Default,
            bodyEmphasized = TextStyle.Default,
            calloutRegular = TextStyle.Default,
            calloutEmphasized = TextStyle.Default,
            subheadlineRegular = TextStyle.Default,
            subheadlineEmphasized = TextStyle.Default,
            footnoteRegular = TextStyle.Default,
            footnoteEmphasized = TextStyle.Default,
            caption1Regular = TextStyle.Default,
            caption1Emphasized = TextStyle.Default,
            caption2Regular = TextStyle.Default,
            caption2Emphasized = TextStyle.Default,
        )
    }
)
