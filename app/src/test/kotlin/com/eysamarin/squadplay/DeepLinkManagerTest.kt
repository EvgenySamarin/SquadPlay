package com.eysamarin.squadplay

import com.eysamarin.squadplay.navigation.DefaultDeepLinkManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinkManagerTest {

    private val deepLinkManager = DefaultDeepLinkManager()

    @Test
    fun setAndConsumePendingInviteGroupId() {
        assertNull(deepLinkManager.pendingInviteGroupId.value)

        deepLinkManager.setPendingInviteGroupId("group-123")
        assertEquals("group-123", deepLinkManager.pendingInviteGroupId.value)

        val consumed = deepLinkManager.consumePendingInviteGroupId()
        assertEquals("group-123", consumed)
        assertNull(deepLinkManager.pendingInviteGroupId.value)
    }

    @Test
    fun extractInviteGroupId_nullUri_returnsNull() {
        assertNull(deepLinkManager.extractInviteGroupId(null))
    }
}
