package com.eysamarin.squadplay.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.DSButton
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.PREVIEW_PROFILE_SCREEN_UI
import com.eysamarin.squadplay.models.ProfileScreenAction
import com.eysamarin.squadplay.models.ProfileScreenUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.UserGroupSection
import com.eysamarin.squadplay.ui.EmptyContent
import com.eysamarin.squadplay.ui.UserAvatar
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveLabelByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(
    state: UiState<ProfileScreenUI>,
    isLoggingOut: Boolean = false,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (ProfileScreenAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(ProfileScreenAction.OnBackButtonTap) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.content_description_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { if (!isLoggingOut) onAction(ProfileScreenAction.OnLogOutTap) },
                        enabled = !isLoggingOut,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_exit_to_app_24),
                            contentDescription = stringResource(R.string.content_description_log_out),
                        )
                    }
                }
            )
        },
        containerColor = DesignSystemTheme.colorScheme.surface,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    UiState.Loading -> {
                        LoadingIndicator()
                    }
                    is UiState.Normal -> {
                        when (windowSize.widthSizeClass) {
                            WindowWidthSizeClass.Expanded,
                            WindowWidthSizeClass.Compact,
                            WindowWidthSizeClass.Medium -> ProfileScreenMediumLayout(
                                state, windowSize, onAction
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    )

    if (isLoggingOut) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DesignSystemTheme.colorScheme.surface.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center,
        ) {
            LoadingIndicator()
        }
    }
}

@Composable
private fun ProfileScreenMediumLayout(
    state: UiState<ProfileScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (ProfileScreenAction) -> Unit,
) {
    if (state !is UiState.Normal) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, false),
            ) {
                Text(
                    text = state.data.user.username,
                    style = adaptiveHeadlineByHeight(windowSize),
                    color = DesignSystemTheme.colorScheme.onSurface
                )
                state.data.user.email?.let {
                    Text(
                        text = it,
                        style = adaptiveTitleByHeight(windowSize),
                        color = DesignSystemTheme.colorScheme.onSurface
                    )
                }
            }

            UserAvatar(
                imageUrl = state.data.user.photoUrl,
            )
        }
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { onAction(ProfileScreenAction.OnSettingsTap) }){
                Icon(
                    painter = painterResource(R.drawable.ic_settings_24),
                    contentDescription = stringResource(R.string.content_description_settings),
                    tint = DesignSystemTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        GroupsList(
            groupSections = state.data.groupSections,
            windowSize = windowSize,
            onAction = onAction,
        )
    }
}

@Composable
private fun GroupsList(
    groupSections: List<UserGroupSection>,
    windowSize: WindowSizeClass,
    onAction: (ProfileScreenAction) -> Unit,
) {
    if (groupSections.isEmpty()) {
        EmptyContent(windowSize, modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupSections.forEach { section ->
            item(key = section.groupId) {
                GroupHeader(
                    title = section.title,
                    groupId = section.groupId,
                    windowSize = windowSize,
                    onAction = onAction,
                )
            }
            if (section.members.isEmpty()) {
                item(key = "${section.groupId}_empty") {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 8.dp),
                        text = stringResource(R.string.no_group_members),
                        style = adaptiveLabelByHeight(windowSize),
                        color = DesignSystemTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(
                    items = section.members,
                    key = { member -> "${section.groupId}_${member.uid}" }
                ) { member ->
                    MemberRow(
                        member = member,
                        windowSize = windowSize,
                    )
                }
            }
            item(key = "${section.groupId}_divider") {
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun GroupHeader(
    title: String,
    groupId: String,
    windowSize: WindowSizeClass,
    onAction: (ProfileScreenAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = adaptiveHeadlineByHeight(windowSize),
            color = DesignSystemTheme.colorScheme.onSurface
        )
        DSButton(
            text = stringResource(R.string.share_invite_link),
            onTap = {
                onAction(ProfileScreenAction.OnCreateInviteLinkTap(groupId))
            }
        )
    }
}

@Composable
private fun MemberRow(
    member: Friend,
    windowSize: WindowSizeClass,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (member.photoUrl != null) {
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High)),
                model = member.photoUrl,
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
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = member.username,
                style = adaptiveTitleByHeight(windowSize),
                color = DesignSystemTheme.colorScheme.onSurface
            )
        }
    }
}

//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun ProfileScreenPhonePreview() {
    DesignSystemTheme {
        ProfileScreen(
            state = UiState.Normal(PREVIEW_PROFILE_SCREEN_UI),
            onAction = {}
        )
    }
}
//endregion