package me.gingerninja.authenticator.core.codegen

import android.os.PowerManager
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.gingerninja.authenticator.core.model.Account
import me.gingerninja.authenticator.core.model.TotpAccount
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

interface CodeGenerator {
    fun getCurrentTime(): Instant

    fun getCode(account: Account): String

    fun getRemainingTime(account: Account): Duration?
}

@Composable
fun rememberCodeGeneratorState(codeGenerator: CodeGenerator): CodeGeneratorState {
    val scope = rememberCoroutineScope()

    return remember(codeGenerator, scope) {
        CodeGeneratorState(codeGenerator, scope)
    }
}

@Composable
fun rememberCodeState(
    generator: CodeGeneratorState,
    account: Account,
): CodeState {
    val context = LocalContext.current

    val isPowerSaving = remember(context) {
        context.getSystemService(PowerManager::class.java)?.isPowerSaveMode ?: false
    }

    val state = remember(account, generator) {
        CodeState(
            account = account,
            initialCode = generator.codeGenerator.getCode(account),
            initialRemainingTime = generator.codeGenerator.getRemainingTime(account)
        )
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(generator, account, lifecycleOwner, isPowerSaving) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            state.code = generator.codeGenerator.getCode(account)
            state.remainingTime = generator.codeGenerator.getRemainingTime(account)

            if (account is TotpAccount) {
                launch {
                    generator.clock.collect {
                        state.code = generator.codeGenerator.getCode(account)

                        if (isPowerSaving) {
                            state.remainingTime = generator.codeGenerator.getRemainingTime(account)
                        }
                    }
                }

                launch {
                    while (!isPowerSaving && isActive) {
                        withInfiniteAnimationFrameMillis {
                            val remaining = generator.codeGenerator.getRemainingTime(account)

                            if ((remaining ?: Duration.ZERO) > (state.remainingTime
                                    ?: Duration.ZERO)
                            ) {
                                state.code = generator.codeGenerator.getCode(account)
                            }

                            state.remainingTime = remaining
                        }
                    }
                }
            }
        }
    }

    return state
}

@Immutable
class CodeGeneratorState(
    internal val codeGenerator: CodeGenerator,
    private val scope: CoroutineScope,
) {
    val clock = flow {
        while (currentCoroutineContext().isActive) {
            emit(codeGenerator.getCurrentTime())
            delay(1000.milliseconds)
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(1000L),
        initialValue = codeGenerator.getCurrentTime(),
    )
}

@Stable
class CodeState(
    account: Account,
    initialCode: String,
    initialRemainingTime: Duration?,
) {
    var code by mutableStateOf(initialCode)
        internal set

    var remainingTime by mutableStateOf(initialRemainingTime)
        internal set

    val remainingTimeFraction by derivedStateOf {
        if (account is TotpAccount) {
            (remainingTime?.inWholeMilliseconds ?: 0) / account.period.times(1000).toFloat()
        } else {
            Float.NaN
        }
    }
}