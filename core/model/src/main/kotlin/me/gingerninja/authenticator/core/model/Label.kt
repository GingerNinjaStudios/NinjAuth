package me.gingerninja.authenticator.core.model

import kotlin.time.Instant

data class Label(
    val id: Long,
    val uid: String,
    val name: String,
    val color: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val icon: String? = null,
    val position: Int = -1,
    val numberOfAccounts: Int? = null,
)