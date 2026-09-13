package com.eysamarin.squadplay.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.models.EventMemberUI
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.models.EventUI

@Composable
fun EventItem(
    event: EventUI,
    modifier: Modifier = Modifier,
    members: List<EventMemberUI> = emptyList(),
    maxVisibleAvatars: Int = 4,
    onDetailsTap: () -> Unit = {},
) {
    val isAllAccepted = if (members.isNotEmpty()) {
        members.all { it.status == EventResponseStatus.ACCEPTED }
    } else {
        event.userStatus == EventResponseStatus.ACCEPTED
    }

    val isAnyRejected = if (members.isNotEmpty()) {
        members.any { it.status == EventResponseStatus.REJECTED }
    } else {
        event.userStatus == EventResponseStatus.REJECTED
    }

    val border = if (isAllAccepted) {
        BorderStroke(2.dp, Color(0xFF4CAF50).copy(alpha = 0.6f))
    } else {
        null
    }

    val (statusIconRes, statusTint) = when {
        isAllAccepted -> R.drawable.ic_check_circle_24 to Color(0xFF4CAF50)
        isAnyRejected -> R.drawable.ic_cancel_24 to Color(0xFFF44336)
        else -> R.drawable.ic_help_24 to Color.Gray
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDetailsTap() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DesignSystemTheme.colorScheme.surfaceContainerHigh
        ),
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game cover image banner
            val coverShape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            if (!event.iconUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = event.iconUrl,
                    contentDescription = event.title,
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight()
                        .clip(coverShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(com.eysamarin.squadplay.designSystem.R.drawable.img_stub),
                    contentDescription = event.title,
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight()
                        .clip(coverShape),
                    tint = Color.Unspecified
                )
            }

            // Event details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title.uppercase(),
                    style = DesignSystemTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystemTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val subtitle = event.subtitle
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = DesignSystemTheme.typography.bodyMedium,
                        color = DesignSystemTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val acceptedCount = members.count { it.status == EventResponseStatus.ACCEPTED }
                    val statusText = if (members.isNotEmpty()) {
                        if (acceptedCount == members.size) {
                            "$acceptedCount/${members.size} confirmed"
                        } else {
                            "$acceptedCount/${members.size} ready"
                        }
                    } else {
                        when (event.userStatus) {
                            EventResponseStatus.ACCEPTED -> stringResource(R.string.content_description_status_accepted)
                            EventResponseStatus.REJECTED -> stringResource(R.string.content_description_status_rejected)
                            EventResponseStatus.NOT_SET -> stringResource(R.string.content_description_status_not_set)
                        }
                    }

                    Icon(
                        painter = painterResource(statusIconRes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = statusTint
                    )

                    Text(
                        text = statusText,
                        style = DesignSystemTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DesignSystemTheme.colorScheme.onSurface
                    )
                }

                // Participant avatars row
                if (members.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        val visibleMembers = members.take(maxVisibleAvatars)
                        val remainingCount = members.size - visibleMembers.size

                        visibleMembers.forEach { member ->
                            MemberAvatarItem(member = member)
                        }

                        if (remainingCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DesignSystemTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$remainingCount",
                                    style = DesignSystemTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystemTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberAvatarItem(
    member: EventMemberUI,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(32.dp)
    ) {
        val avatarShape = CircleShape
        if (!member.photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = member.photoUrl,
                contentDescription = member.username,
                modifier = Modifier
                    .size(30.dp)
                    .clip(avatarShape)
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.default_avatar),
                contentDescription = member.username,
                modifier = Modifier
                    .size(30.dp)
                    .clip(avatarShape)
                    .align(Alignment.Center),
                tint = Color.Unspecified
            )
        }

        val badgeColor = when (member.status) {
            EventResponseStatus.ACCEPTED -> Color(0xFF4CAF50)
            EventResponseStatus.REJECTED -> Color(0xFFF44336)
            EventResponseStatus.NOT_SET -> Color.Gray
        }

        val badgeIcon = when (member.status) {
            EventResponseStatus.ACCEPTED -> R.drawable.ic_check_circle_24
            EventResponseStatus.REJECTED -> R.drawable.ic_cancel_24
            EventResponseStatus.NOT_SET -> R.drawable.ic_help_24
        }

        Box(
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(badgeColor)
                .border(1.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(badgeIcon),
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = Color.White
            )
        }
    }
}

@DarkLightModePreview
@Composable
private fun EventItemPreview() {
    val sampleEvent = EventUI(
        eventId = "1",
        title = "APEX LEGENDS - RANKED GRIND",
        subtitle = "20:00 - 23:00",
        iconUrl = null,
        userStatus = EventResponseStatus.ACCEPTED
    )

    val confirmedMembers = listOf(
        EventMemberUI("1", "User 1", null, EventResponseStatus.ACCEPTED),
        EventMemberUI("2", "User 2", null, EventResponseStatus.ACCEPTED),
        EventMemberUI("3", "User 3", null, EventResponseStatus.ACCEPTED),
        EventMemberUI("4", "User 4", null, EventResponseStatus.ACCEPTED),
    )

    val rejectedMembers = listOf(
        EventMemberUI("1", "User 1", null, EventResponseStatus.ACCEPTED),
        EventMemberUI("2", "User 2", null, EventResponseStatus.REJECTED),
        EventMemberUI("3", "User 3", null, EventResponseStatus.ACCEPTED),
        EventMemberUI("4", "User 4", null, EventResponseStatus.ACCEPTED),
    )

    val pendingMembers = listOf(
        EventMemberUI("1", "User 1", null, EventResponseStatus.ACCEPTED),
        EventMemberUI("2", "User 2", null, EventResponseStatus.NOT_SET),
        EventMemberUI("3", "User 3", null, EventResponseStatus.ACCEPTED),
        EventMemberUI("4", "User 4", null, EventResponseStatus.ACCEPTED),
    )

    DesignSystemTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EventItem(
                event = sampleEvent,
                members = confirmedMembers
            )
            EventItem(
                event = sampleEvent,
                members = rejectedMembers
            )
            EventItem(
                event = sampleEvent,
                members = pendingMembers
            )
        }
    }
}
