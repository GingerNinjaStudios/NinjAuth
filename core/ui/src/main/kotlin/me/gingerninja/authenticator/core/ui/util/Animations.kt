package me.gingerninja.authenticator.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive

@Composable
fun rememberAnimatedSystemTime(): State<Long> {
    val currentTime = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            awaitFrame()
            currentTime.longValue = System.currentTimeMillis()
        }
    }

    return currentTime
}

fun periodToProgress(currentTime: Long, periodInMillis: Long): Float {
    return (currentTime.mod(periodInMillis) / periodInMillis.toFloat())
}