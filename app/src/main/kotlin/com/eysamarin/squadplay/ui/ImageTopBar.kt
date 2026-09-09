package com.eysamarin.squadplay.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eysamarin.squadplay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageTopBar(
    imageUrl: String?,
    onBackTap: () -> Unit,
    modifier: Modifier = Modifier,
    headerHeight: Dp = 220.dp,
    contentDescription: String? = "Game Thumbnail",
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        AsyncImage(
            model = imageUrl,
            placeholder = painterResource(R.drawable.placeholder),
            fallback = painterResource(R.drawable.placeholder),
            error = painterResource(R.drawable.placeholder),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.55f
                        )
                    )
                },
            contentScale = ContentScale.Crop
        )
    }

    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = onBackTap
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24),
                    contentDescription = stringResource(R.string.content_description_back),
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        windowInsets = WindowInsets.statusBars
    )
}
