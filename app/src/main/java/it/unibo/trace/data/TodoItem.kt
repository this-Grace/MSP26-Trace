package it.unibo.trace.data

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: Long? = null,
    val name: String,
    val uid: String? = null
)

