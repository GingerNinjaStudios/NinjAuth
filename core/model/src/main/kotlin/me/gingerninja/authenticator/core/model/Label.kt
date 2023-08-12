package me.gingerninja.authenticator.core.model

data class Label(
    val id: Long,
    val uid: String,
    val name: String,
    val color: Long,
    val icon: String? = null,
    val position: Int = -1,
    val numberOfAccounts: Int? = null
)