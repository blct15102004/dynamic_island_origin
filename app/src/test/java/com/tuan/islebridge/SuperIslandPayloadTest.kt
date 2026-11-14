package com.tuan.islebridge

import com.tuan.islebridge.core.common.Constants
import com.tuan.islebridge.core.originisland.*
import org.junit.Assert.*
import org.junit.Test

class SuperIslandPayloadTest {

    @Test
    fun testPayloadDataStructure() {
        val payload = SuperIslandPayload(
            eventId = "event_101",
            operation = SuperIslandOperation.MOUNT,
            templateId = Constants.SuperXProtocol.TEMPLATE_PROGRESS,
            scene = IslandScene.MEDIA,
            title = "Now Playing",
            content = "Artist - Track",
            capsule = CapsulePayload(
                text = "Playing",
                subText = "Track"
            ),
            showNotify = true,
            isIsland = true,
            progress = 50,
            maxProgress = 100,
            baseInfos = listOf(BaseInfoItem("artist", "Artist")),
            infos = listOf("Line 1", "Line 2"),
            shortInfos = "Now Playing"
        )

        assertEquals("event_101", payload.eventId)
        assertEquals(SuperIslandOperation.MOUNT, payload.operation)
        assertEquals(Constants.SuperXProtocol.OP_MOUNT, payload.operation.code)
        assertEquals(Constants.SuperXProtocol.TEMPLATE_PROGRESS, payload.templateId)
        assertEquals(IslandScene.MEDIA, payload.scene)
        assertEquals(50, payload.progress)
        assertEquals(100, payload.maxProgress)
        assertEquals("Playing", payload.capsule.text)
        assertEquals("Track", payload.capsule.subText)
    }

    @Test
    fun testUnmountOperation() {
        val unmount = SuperIslandOperation.UNMOUNT
        assertEquals(Constants.SuperXProtocol.OP_UNMOUNT, unmount.code)
    }
}

