package me.gingerninja.authenticator.core.model.settings

data class AppearanceConfig(
    val theme: Theme,
) {
    enum class Theme(val value: String) {
        DARK("dark"),

        LIGHT("light"),

        BATTERY_SAVER("battery"),

        // requires API 28
        SYSTEM("system");

        companion object {
            fun fromString(data: String?): Theme? = Theme.values().find { it.value == data }
        }
    }
}