package me.gingerninja.authenticator.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import kotlinx.serialization.Serializable
import me.gingerninja.authenticator.core.model.settings.SecurityConfig

@Serializable
sealed interface NinjaScreen : NavKey {
    val isSecure: Boolean get() = true

    val popWhenScreenLocked: Boolean get() = false

    @Serializable
    data class Auth(val isReauthenticating: Boolean = false) : NinjaScreen {
        override val isSecure = false

    }

    @Serializable
    data object Accounts : NinjaScreen

    /*@Serializable
    data object Settings : NinjaScreen*/

    @Serializable
    sealed interface Settings : NinjaScreen {
        @Serializable
        data object Main : Settings


        /*@Serializable
        data class PreAuth(
            val target: Target,
        ) : Settings {
            override val popWhenScreenLocked = true

            enum class Target {
                LOCK_TYPE {
                    override fun createNavKey(oldPassword: ByteArray) = TODO()
                },

                BIOMETRIC {
                    override fun createNavKey(oldPassword: ByteArray) = Biometric(
                        oldPassword = oldPassword,
                    )
                };

                abstract fun createNavKey(oldPassword: ByteArray): NinjaScreen
            }
        }*/

        @Serializable
        data class PasswordSet(
            val lockType: SecurityConfig.LockType,
        ) : Settings, PreAuthRequired {
            override val popWhenScreenLocked = true
        }

        @Serializable
        data class LockTypeChooser(
            val lockType: SecurityConfig.LockType,
        ) : Settings, PreAuthRequired {
            override val popWhenScreenLocked = true
        }

        @Serializable
        data class Biometric(
            val lockType: SecurityConfig.LockType,
        ) : Settings, PreAuthRequired {
            override val popWhenScreenLocked = true
        }
    }
}

interface PreAuthRequired

object PreAuthMetadata {
    object ViewModelKey : NavMetadataKey<String>

    const val VIEW_MODEL_VALUE = "securitySettings"
}
