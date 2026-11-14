package com.tuan.islebridge.core.originisland

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

class IslandPriorityQueue {

    private val cardMap = ConcurrentHashMap<String, IslandCardModel>()
    private val priorityQueue = PriorityBlockingQueue<IslandCardModel>()

    private val _activeCardsFlow = MutableStateFlow<List<IslandCardModel>>(emptyList())
    val activeCardsFlow: StateFlow<List<IslandCardModel>> = _activeCardsFlow.asStateFlow()

    @Synchronized
    fun postOrUpdate(card: IslandCardModel) {
        val existing = cardMap[card.id]
        if (existing != null) {
            priorityQueue.remove(existing)
        }
        cardMap[card.id] = card
        priorityQueue.offer(card)
        updateStateFlow()
    }

    @Synchronized
    fun remove(cardId: String): IslandCardModel? {
        val existing = cardMap.remove(cardId)
        if (existing != null) {
            priorityQueue.remove(existing)
            updateStateFlow()
        }
        return existing
    }

    @Synchronized
    fun getTopCard(): IslandCardModel? {
        return priorityQueue.peek()
    }

    @Synchronized
    fun getAllCards(): List<IslandCardModel> {
        return priorityQueue.toList().sorted()
    }

    @Synchronized
    fun cleanExpiredCards(currentTimeMs: Long = System.currentTimeMillis()): List<IslandCardModel> {
        val expired = mutableListOf<IslandCardModel>()
        val iterator = priorityQueue.iterator()
        while (iterator.hasNext()) {
            val card = iterator.next()
            if (card.timeoutMs > 0 && (currentTimeMs - card.timestamp) > card.timeoutMs) {
                iterator.remove()
                cardMap.remove(card.id)
                expired.add(card)
            }
        }
        if (expired.isNotEmpty()) {
            updateStateFlow()
        }
        return expired
    }

    @Synchronized
    fun clear() {
        cardMap.clear()
        priorityQueue.clear()
        updateStateFlow()
    }

    private fun updateStateFlow() {
        _activeCardsFlow.value = priorityQueue.toList().sorted()
    }
}

