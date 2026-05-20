package me.gingerninja.authenticator

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.model.settings.AppearanceConfig
import me.gingerninja.authenticator.core.model.settings.SecurityConfig.Companion.ImmediateLock
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val settings: NinjAuthSettings,
    private val dbAuthenticator: NinjAuthDatabaseAuthenticator
) : ViewModel() {
    private var lockScreenStartTime: Long
        get() {
            return savedStateHandle[SAVED_STATE_LOCK_START_TIME_KEY] ?: Long.MIN_VALUE
        }
        set(value) {
            savedStateHandle[SAVED_STATE_LOCK_START_TIME_KEY] = value
        }

    val showLockScreen: ReceiveChannel<Unit>
        field = Channel(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val state: StateFlow<MainUiState> = settings.data.map { settings ->
        MainUiState(
            theme = settings.appearance.theme,
            dynamicColors = settings.appearance.dynamicColors,
            hideRecents = settings.security.hideRecent,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = MainUiState(),
        )

    internal fun startLockScreenCounter(forceReset: Boolean) {
        val security = runBlocking {
            settings.data.firstOrNull()?.security ?: NinjAuthSettings.Defaults.security
        }

        lockScreenStartTime = when {
            forceReset -> Long.MIN_VALUE
            security.lockLeave == ImmediateLock -> {
                lockApp()

                Long.MIN_VALUE
            }

            security.shouldLockWhenLeave -> SystemClock.elapsedRealtime()
            else -> Long.MIN_VALUE
        }
    }


    internal fun stopLockScreenCounter(): Boolean {
        if (lockScreenStartTime >= 0) {
            val security = runBlocking {
                settings.data.firstOrNull()?.security ?: NinjAuthSettings.Defaults.security
            }

            val diff = (SystemClock.elapsedRealtime() - lockScreenStartTime).milliseconds

            return (diff > security.lockLeave).apply {
                if (this) {
                    lockApp()
                }
            }
        }

        return false
    }

    private fun lockApp() {
        showLockScreen.trySend(Unit)
        dbAuthenticator.close()
    }

    companion object {
        private const val SAVED_STATE_LOCK_START_TIME_KEY = "lockTimer"
    }
}

data class MainUiState(
    val theme: AppearanceConfig.Theme = NinjAuthSettings.Defaults.appearance.theme,
    val dynamicColors: Boolean = NinjAuthSettings.Defaults.appearance.dynamicColors,
    val hideRecents: Boolean = NinjAuthSettings.Defaults.security.hideRecent,
)