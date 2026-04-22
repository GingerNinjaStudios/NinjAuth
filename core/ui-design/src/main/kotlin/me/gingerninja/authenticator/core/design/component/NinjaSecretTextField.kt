package me.gingerninja.authenticator.core.design.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import me.gingerninja.authenticator.core.ui.design.R

@Composable
fun rememberSecretTextFieldState(): NinjaSecretTextFieldState {
    return remember {
        NinjaSecretTextFieldState()
    }
}

@Composable
fun NinjaSecretTextField(
    modifier: Modifier = Modifier,
    state: NinjaSecretTextFieldState,
    textAlign: TextAlign? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
) {
    NinjaSecretTextField(
        modifier = modifier,
        value = state.value,
        onValueChange = { state.value = it },
        showSecret = state.showSecret,
        onToggleSecret = { state.showSecret = it },
        textAlign = textAlign,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

@Composable
fun NinjaSecretTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    showSecret: Boolean = false,
    onToggleSecret: ((Boolean) -> Unit)? = null,
    textAlign: TextAlign? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
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

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        supportingText = {
            supportingText?.let {
                Text(text = it)
            }
        },
        textStyle = textStyle,
        visualTransformation = if (showSecret) VisualTransformation.None else passwordTransformation,
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
        keyboardActions = keyboardActions,
        singleLine = true
    )
}

@Stable
class NinjaSecretTextFieldState {
    var value by mutableStateOf("")

    var showSecret by mutableStateOf(false)
}

private val passwordTransformation = PasswordVisualTransformation()