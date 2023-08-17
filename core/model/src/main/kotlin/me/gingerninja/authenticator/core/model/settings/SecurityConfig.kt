package me.gingerninja.authenticator.core.model.settings

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class SecurityConfig(
    /**
     * The lock type.
     */
    val lockType: LockType,

    /**
     * The app gets locked when the user leaves the app for this amount of time.
     */
    val lockLeave: Duration,

    /**
     * Whether biometrics are enabled.
     */
    val isBiometricsEnabled: Boolean,

    /**
     * Whether to hide the app from recents screen. Cannot take screenshots if this is enabled.
     */
    val hideRecent: Boolean,

    /**
     * The version of biometrics security used. This is reserved for later use when / if biometric
     * authentication changes.
     */
    val biometricsVersion: Int?,
) {
    val shouldLockWhenLeave: Boolean get() = !lockLeave.isNegative()

    enum class LockType(val value: String) {
        NONE("none"), PIN("pin"), PASSWORD("password");

        companion object {
            fun fromString(data: String?): LockType? = LockType.values().find { it.value == data }
        }
    }

    companion object {
        val NeverLock = (-1).seconds
    }
}