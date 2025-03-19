package com.eysamarin.squadplay.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient1
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient2
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient3

@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
) {
    Box(
        modifier = modifier
            .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High))
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AvatarBorderGradient1,
                        AvatarBorderGradient2,
                        AvatarBorderGradient3
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                ),
                shape = SquircleShape(cornerSmoothing = CornerSmoothing.High)
            )
            .padding(4.dp)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High)),
                model = imageUrl,
                contentDescription = null,
            )
        } else {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High)),
                painter = painterResource(R.drawable.default_avatar),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}

@Preview
@Composable
private fun UserAvatarPreview() {
    UserAvatar()
}