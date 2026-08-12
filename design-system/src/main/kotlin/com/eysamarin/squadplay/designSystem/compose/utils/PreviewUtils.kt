package com.eysamarin.squadplay.designSystem.compose.utils

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.designSystem.R


@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class DarkLightModePreview

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
    name = "Tablet preview light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class TabletLightModePreview

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
    name = "Tablet preview dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class TabletDarkModePreview

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp",
    name = "Phone preview light",
    locale = "en",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class PhoneLightModePreview


@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp",
    name = "Phone preview night",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class PhoneDarkModePreview

@Preview(
    showBackground = true,
    device = "id:wearos_rect",
    name = "Phone preview light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class WearLightModePreview

@Preview(
    showBackground = true,
    device = "id:wearos_rect",
    name = "Phone preview dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class WearDarkModePreview

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
object PreviewUtils {
    /**
     *  - width < 600.dp
     *  - height < 480.dp
     */
    val WINDOWS_SIZE_COMPACT = WindowSizeClass.calculateFromSize(
        DpSize(width = 300.dp, height = 400.dp)
    )

    /**
     * - width < 840.dp
     * - height < 900.dp
     */
    val WINDOWS_SIZE_MEDIUM = WindowSizeClass.calculateFromSize(
        DpSize(width = 700.dp, height = 800.dp)
    )

    /**
     * - width >= 840.dp
     * - height >= 900.dp
     */
    val WINDOWS_SIZE_EXPANDED = WindowSizeClass.calculateFromSize(
        DpSize(width = 900.dp, height = 1000.dp)
    )
}

@Composable
fun previewIconPainter(): Painter = painterResource(R.drawable.ic24_select)

@Composable
fun previewImagePainter(): Painter = painterResource(R.drawable.img_stub)

@Composable
fun VariantPreviewText(text: String) = Text(
    text = text,
    modifier = Modifier
        .fillMaxWidth()
        .background(Color.Cyan)
)

@Suppress("unused")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun previewSheetState(): SheetState = remember {
    SheetState(
        skipPartiallyExpanded = true,
        positionalThreshold = { .1f },
        velocityThreshold = { .1f },
        initialValue = SheetValue.Expanded,
    )
}

@Suppress("unused")
fun loremIpsumString(words: Int): String = LoremIpsum(words).values.joinToString { it }
