package me.gingerninja.authenticator.core.design.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import me.gingerninja.authenticator.core.ui.design.R

@Composable
fun NinjaPasswordField(
    state: NinjaSecretTextFieldState,
    enabled: Boolean,
    isError: Boolean,
    isPin: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    label: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onSubmit: (() -> Unit)?,
) {
    val keyboardOptions = remember(isPin, imeAction) {
        val keyboardType = when (isPin) {
            true -> KeyboardType.NumberPassword
            false -> KeyboardType.Password
        }

        KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction)
    }

    val [showError, setShowError] = rememberSaveable(isError) {
        mutableStateOf(isError)
    }

    DisposableEffect(state.value) {
        setShowError(false)
        onDispose { }
    }

    val errorTextRes = remember(showError, isPin) {
        if (showError) {
            when (isPin) {
                true -> R.string.auth_error_wrong_pin
                false -> R.string.auth_error_wrong_password
            }
        } else {
            null
        }
    }

    NinjaSecretTextField(
        modifier = modifier,
        state = state,
        enabled = enabled,
        isError = showError,
        textAlign = if (isPin) TextAlign.Center else TextAlign.Start,
        label = label,
        supportingText = errorTextRes?.let { stringResource(id = it) }
            ?: "", // intentionally using empty string as placeholder
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onSubmit?.let {
            {
                if (state.value.isNotEmpty()) {
                    state.showSecret = false
                    focusRequester.freeFocus()
                    onSubmit()
                }
            }
        }
    )
}

@Composable
fun NinjaPasswordField(
    state: NinjaSecretTextFieldState,
    enabled: Boolean,
    isPin: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    label: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onSubmit: (() -> Unit)?,
    error: String? = null,
) {
    val keyboardOptions = remember(isPin, imeAction) {
        val keyboardType = when (isPin) {
            true -> KeyboardType.NumberPassword
            false -> KeyboardType.Password
        }

        KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction)
    }

    NinjaSecretTextField(
        modifier = modifier,
        state = state,
        enabled = enabled,
        isError = error != null,
        textAlign = if (isPin) TextAlign.Center else TextAlign.Start,
        label = label,
        supportingText = error ?: "", // intentionally using empty string as placeholder
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onSubmit?.let {
            {
                if (state.value.isNotEmpty()) {
                    state.showSecret = false
                    focusRequester.freeFocus()
                    onSubmit()
                }
            }
        }
    )
}