package com.tuan.islebridge.core.originisland

import android.app.PendingIntent

enum class CardPriority(val value: Int) {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4) // e.g. OTP, Privacy alerts
}

data class IslandCardModel(
    val id: String,
    val sourcePackage: String,
    val scene: IslandScene,
    val title: String,
    val subtitle: String,
    val capsuleText: String,
    val capsuleSubtext: String? = null,
    val progress: Int? = null,
    val maxProgress: Int? = null,
    val isIndeterminate: Boolean = false,
    val priority: CardPriority = CardPriority.NORMAL,
    val timestamp: Long = System.currentTimeMillis(),
    val timeoutMs: Long = 10000L,
    val actionLabel: String? = null,
    val actionIntent: PendingIntent? = null,
    val extraDetails: Map<String, String> = emptyMap()
) : Comparable<IslandCardModel> {
    override fun compareTo(other: IslandCardModel): Int {
        val priorityComparison = other.priority.value.compareTo(this.priority.value)
        return if (priorityComparison != 0) priorityComparison else other.timestamp.compareTo(this.timestamp)
    }
}

