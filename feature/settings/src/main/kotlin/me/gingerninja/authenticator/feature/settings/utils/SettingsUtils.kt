package me.gingerninja.authenticator.feature.settings.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.core.model.settings.SecurityConfig.Companion.ImmediateLock
import me.gingerninja.authenticator.core.model.settings.SecurityConfig.Companion.NeverLock
import me.gingerninja.authenticator.feature.settings.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal val SecurityConfig.LockType.titleRes: Int
    get() = when (this) {
        SecurityConfig.LockType.NONE -> R.string.settings_security_lock_type_none
        SecurityConfig.LockType.PIN -> R.string.settings_security_lock_type_pin
        SecurityConfig.LockType.PASSWORD -> R.string.settings_security_lock_type_password
    }

@Composable
internal fun Duration.getLockoutTitle(): String {
    return when {
        this == NeverLock -> stringResource(R.string.settings_security_lock_timeout_value_never)
        this == ImmediateLock -> stringResource(R.string.settings_security_lock_timeout_value_immediately)
        this < 60.seconds -> pluralStringResource(
            R.plurals.settings_security_lock_timeout_value_seconds,
            inWholeSeconds.toInt(),
            inWholeSeconds.toInt()
        )

        else -> pluralStringResource(
            R.plurals.settings_security_lock_timeout_value_minutes,
            inWholeMinutes.toInt(),
            inWholeMinutes.toInt()
        )
    }
}