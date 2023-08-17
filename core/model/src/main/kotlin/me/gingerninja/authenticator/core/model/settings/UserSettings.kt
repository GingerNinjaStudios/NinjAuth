package me.gingerninja.authenticator.core.model.settings

data class UserSettings(
    val appearance: AppearanceConfig,
    val security: SecurityConfig,
    val firstRunComplete: Boolean,
)