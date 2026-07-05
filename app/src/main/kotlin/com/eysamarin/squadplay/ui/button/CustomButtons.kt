package com.eysamarin.squadplay.ui.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview

/**
 * @see <a href="https://developers.google.com/identity/branding-guidelines">
 *     Google Identity Guidelines</a>
 */
@Composable
fun GoogleButton(
    text: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        enabled = enabled,
        onClick = onTap,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                DesignSystemTheme.colorScheme.inverseSurface
            } else {
                DesignSystemTheme.colorScheme.outline
            }
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = DesignSystemTheme.colorScheme.surface,
            contentColor = DesignSystemTheme.colorScheme.onSurface,
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google_20),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text)
    }
}

@DarkLightModePreview
@Composable
fun ButtonsPreview() {
    DesignSystemTheme {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .padding(16.dp)
        ) {
            GoogleButton(text = "Sign in with Google", {})
        }
    }
}