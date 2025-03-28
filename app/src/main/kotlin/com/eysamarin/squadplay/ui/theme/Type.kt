package com.eysamarin.squadplay.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


fun adaptiveHeadlineByHeight(windowSizeClass: WindowSizeClass): TextStyle {
    return when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Expanded -> Typography.headlineLarge
        WindowHeightSizeClass.Medium -> Typography.headlineMedium
        WindowHeightSizeClass.Compact -> Typography.headlineSmall
        else -> Typography.headlineMedium
    }
}

fun adaptiveTitleByHeight(windowSizeClass: WindowSizeClass): TextStyle {
    return when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Expanded -> Typography.titleLarge
        WindowHeightSizeClass.Medium -> Typography.titleMedium
        WindowHeightSizeClass.Compact -> Typography.titleSmall
        else -> Typography.titleMedium
    }
}

fun adaptiveLabelByHeight(windowSizeClass: WindowSizeClass): TextStyle {
    return when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Expanded -> Typography.labelLarge
        WindowHeightSizeClass.Medium -> Typography.labelMedium
        WindowHeightSizeClass.Compact -> Typography.labelSmall
        else -> Typography.labelMedium
    }
}

fun adaptiveBodyByHeight(windowSizeClass: WindowSizeClass): TextStyle {
    return when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Expanded -> Typography.bodyLarge
        WindowHeightSizeClass.Medium -> Typography.bodyMedium
        WindowHeightSizeClass.Compact -> Typography.bodySmall
        else -> Typography.bodyMedium
    }
}

val Typography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)