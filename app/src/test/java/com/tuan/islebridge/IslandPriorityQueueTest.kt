package com.tuan.islebridge

import com.tuan.islebridge.core.originisland.CardPriority
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.originisland.IslandPriorityQueue
import com.tuan.islebridge.core.originisland.IslandScene
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class IslandPriorityQueueTest {

    private lateinit var queue: IslandPriorityQueue

    @Before
    fun setup() {
        queue = IslandPriorityQueue()
    }

    @Test
    fun testPrioritySorting_UrgentFirst() {
        val normalCard = IslandCardModel(
            id = "normal_1",
            sourcePackage = "com.spotify.music",
            scene = IslandScene.MEDIA,
            title = "Spotify",
            subtitle = "Song",
            capsuleText = "Music",
            priority = CardPriority.NORMAL,
            timestamp = 1000L
        )

        val urgentCard = IslandCardModel(
            id = "urgent_1",
            sourcePackage = "com.google.android.apps.messaging",
            scene = IslandScene.OTP,
            title = "OTP",
            subtitle = "Code",
            capsuleText = "OTP: 1234",
            priority = CardPriority.URGENT,
            timestamp = 1005L
        )

        queue.postOrUpdate(normalCard)
        queue.postOrUpdate(urgentCard)

        val topCard = queue.getTopCard()
        assertNotNull(topCard)
        assertEquals("urgent_1", topCard?.id)
    }

    @Test
    fun testUpdateExistingCard_RetainsSingleEntry() {
        val cardV1 = IslandCardModel(
            id = "dl_1",
            sourcePackage = "com.download",
            scene = IslandScene.PROGRESS,
            title = "File.apk",
            subtitle = "30%",
            capsuleText = "30%",
            progress = 30,
            maxProgress = 100,
            priority = CardPriority.NORMAL
        )

        val cardV2 = cardV1.copy(subtitle = "80%", capsuleText = "80%", progress = 80)

        queue.postOrUpdate(cardV1)
        assertEquals(1, queue.getAllCards().size)

        queue.postOrUpdate(cardV2)
        assertEquals(1, queue.getAllCards().size)
        assertEquals(80, queue.getTopCard()?.progress)
    }

    @Test
    fun testCardExpirationCleanup() {
        val now = 100000L
        val expiredCard = IslandCardModel(
            id = "expired",
            sourcePackage = "com.test",
            scene = IslandScene.SYSTEM,
            title = "Old",
            subtitle = "Expired",
            capsuleText = "Old",
            timestamp = now - 20000L,
            timeoutMs = 10000L
        )

        val activeCard = IslandCardModel(
            id = "active",
            sourcePackage = "com.test",
            scene = IslandScene.SYSTEM,
            title = "Fresh",
            subtitle = "Active",
            capsuleText = "Fresh",
            timestamp = now - 2000L,
            timeoutMs = 10000L
        )

        queue.postOrUpdate(expiredCard)
        queue.postOrUpdate(activeCard)
        assertEquals(2, queue.getAllCards().size)

        val cleaned = queue.cleanExpiredCards(currentTimeMs = now)
        assertEquals(1, cleaned.size)
        assertEquals("expired", cleaned[0].id)
        assertEquals(1, queue.getAllCards().size)
        assertEquals("active", queue.getAllCards()[0].id)
    }
}

