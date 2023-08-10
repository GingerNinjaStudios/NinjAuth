package me.gingerninja.authenticator.model

data class Label(
    val id: Long,
    val uid: String,
    val name: String,
    val color: Long,
    val icon: String? = null,
    val position: Int = -1
)