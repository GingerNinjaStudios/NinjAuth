package me.gingerninja.authenticator.core.design.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import me.gingerninja.authenticator.core.ui.design.R

@Composable
fun rememberSecretTextFieldState(
    initialText: String = "",
): NinjaSecretTextFieldState {
    val textFieldState = rememberTextFieldState(
        initialText = initialText,
    )

    return remember(textFieldState) {
        NinjaSecretTextFieldState(textFieldState)
    }
}

@Composable
fun NinjaSecretTextField(
    modifier: Modifier = Modifier,
    state: NinjaSecretTextFieldState,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: (() -> Unit)?,
    textAlign: TextAlign? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: String? = null,
    supportingText: String? = null,
) {
    NinjaSecretTextField(
        modifier = modifier,
        textFieldState = state.textFieldState,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        showSecret = state.showSecret,
        onToggleSecret = { state.showSecret = it },
        textAlign = textAlign,
        enabled = enabled,
        isError = isError,
        label = label,
        supportingText = supportingText,
    )
}

@Composable
fun NinjaSecretTextField(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: (() -> Unit)?,
    showSecret: Boolean = false,
    onToggleSecret: ((Boolean) -> Unit)? = null,
    textAlign: TextAlign? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: String? = null,
    supportingText: String? = null,
) {
    val localStyle = LocalTextStyle.current

    val textStyle = remember(localStyle, textAlign) {
        textAlign?.let {
            localStyle.copy(textAlign = it)
        } ?: localStyle
    }

    val icon = if (showSecret) {
        R.drawable.ic_visibility_off
    } else {
        R.drawable.ic_visibility_on
    }

    OutlinedSecureTextField(
        modifier = modifier,
        state = textFieldState,
        enabled = enabled,
        isError = isError,
        supportingText = {
            supportingText?.let {
                Text(text = it)
            }
        },
        label = label?.let {
            @Composable {
                Text(text = it)
            }
        },
        labelPosition = TextFieldLabelPosition.Above(),
        textStyle = textStyle,
        textObfuscationMode = if (showSecret) {
            TextObfuscationMode.Visible
        } else {
            TextObfuscationMode.RevealLastTyped
        },
        leadingIcon = if (textAlign == TextAlign.Center) {
            @Composable { } // add a leading empty space so the text is in the center of the field
        } else {
            null
        },
        trailingIcon = {
            if (onToggleSecret != null) {
                IconButton(onClick = { onToggleSecret(!showSecret) }) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction?.let {
            { it() }
        },
    )
}

@Stable
class NinjaSecretTextFieldState(
    val textFieldState: TextFieldState,
) {
    val value by derivedStateOf {
        textFieldState.text
    }

    var showSecret by mutableStateOf(false)
}