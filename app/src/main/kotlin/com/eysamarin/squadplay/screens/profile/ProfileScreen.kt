package com.eysamarin.squadplay.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.PREVIEW_PROFILE_SCREEN_UI
import com.eysamarin.squadplay.models.ProfileScreenAction
import com.eysamarin.squadplay.models.ProfileScreenUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.ui.EmptyContent
import com.eysamarin.squadplay.ui.UserAvatar
import com.eysamarin.squadplay.ui.button.PrimaryButton
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: UiState<ProfileScreenUI>,
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                        )
                    }
                })
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                when (windowSize.widthSizeClass) {
                    WindowWidthSizeClass.Expanded,
                    WindowWidthSizeClass.Compact,
                    WindowWidthSizeClass.Medium -> ProfileScreenMediumLayout(
                        state, windowSize, onAction
                    )
                }
            }
        }
    )
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                state.data.user.email?.let {
                    Text(
                        text = it,
                        style = adaptiveTitleByHeight(windowSize),
                        color = MaterialTheme.colorScheme.onSurface
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
            PrimaryButton(windowSize, text = "Share invite link", onTap = {
                onAction(ProfileScreenAction.OnCreateInviteLinkTap)
            })
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, false),
            text = "Friends list:",
            style = adaptiveBodyByHeight(windowSize),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FriendsList(state.data.friends, windowSize)
    }
}

@Composable
private fun FriendsList(friends: List<Friend>, windowSize: WindowSizeClass) {
    if (friends.isEmpty()) {
        EmptyContent(windowSize, modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(friends) { friend ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = friend.username,
                style = adaptiveBodyByHeight(windowSize),
            )
        }
    }
}

//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun ProfileScreenPhonePreview() {
    SquadPlayTheme {
        ProfileScreen(
            state = UiState.Normal(PREVIEW_PROFILE_SCREEN_UI),
            onAction = {}
        )
    }
}
//endregion