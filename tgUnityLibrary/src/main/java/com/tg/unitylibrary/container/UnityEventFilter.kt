package com.tg.unitylibrary.container

import java.util.*

class UnityEventFilter {
    private val actions: MutableSet<String> = HashSet()
    fun addAction(action: String) {
        actions.add(action)
    }

    fun actionIterator(): Iterator<String> {
        return actions.iterator()
    }
}