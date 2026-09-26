package com.eysamarin.squadplay.screens.security

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.ButtonSize
import com.eysamarin.squadplay.designSystem.compose.ButtonStyle
import com.eysamarin.squadplay.designSystem.compose.DSButton
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview

@Composable
fun SecurityVerificationErrorScreen(
    onRetryTap: () -> Unit,
    onOpenPlayStoreTap: () -> Unit,
    onExitAppTap: () -> Unit,
    modifier: Modifier = Modifier,
    isRetrying: Boolean = false,
) {
    BackHandler(enabled = true) {
        // Disallow navigating back to any previous screen while integrity verification has failed
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DesignSystemTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DesignSystemTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gpp_bad_24),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = DesignSystemTheme.colorScheme.onErrorContainer,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Security Verification Failed",
                style = DesignSystemTheme.typography.headlineSmall,
                color = DesignSystemTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your device and app environment could not be verified. To protect your account and data against modifications and automated threats, access has been temporarily restricted.",
                style = DesignSystemTheme.extendedTypography.subheadlineRegular,
                color = DesignSystemTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = DesignSystemTheme.shapes.medium,
                color = DesignSystemTheme.colorScheme.surfaceVariant,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Suggested steps to resolve:",
                        style = DesignSystemTheme.extendedTypography.bodyEmphasized,
                        color = DesignSystemTheme.colorScheme.onSurfaceVariant,
                    )
                    GuidanceRow(text = "Install the official build from Google Play.")
                    GuidanceRow(text = "Update Google Play Services to the latest version.")
                    GuidanceRow(text = "Check your network connection and disable VPNs or integrity bypasses.")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DSButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (isRetrying) "Verifying..." else "Retry",
                    enabled = !isRetrying,
                    variant = ButtonStyle.Filled,
                    size = ButtonSize.Default,
                    onTap = onRetryTap,
                )

                DSButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Open Google Play",
                    variant = ButtonStyle.Outline,
                    size = ButtonSize.Default,
                    onTap = onOpenPlayStoreTap,
                )

                DSButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Exit App",
                    variant = ButtonStyle.Text,
                    size = ButtonSize.Default,
                    onTap = onExitAppTap,
                )
            }
        }
    }
}

@Composable
private fun GuidanceRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "•",
            style = DesignSystemTheme.extendedTypography.subheadlineRegular,
            color = DesignSystemTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = DesignSystemTheme.extendedTypography.subheadlineRegular,
            color = DesignSystemTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PhoneLightModePreview
@PhoneDarkModePreview
@Composable
private fun SecurityVerificationErrorScreenPreview() {
    DesignSystemTheme {
        SecurityVerificationErrorScreen(
            onRetryTap = {},
            onOpenPlayStoreTap = {},
            onExitAppTap = {},
        )
    }
}
