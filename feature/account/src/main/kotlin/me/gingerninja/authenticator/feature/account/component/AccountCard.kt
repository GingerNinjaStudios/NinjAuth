package me.gingerninja.authenticator.feature.account.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.gingerninja.authenticator.core.codegen.CodeGeneratorState
import me.gingerninja.authenticator.core.codegen.OtpGenerator
import me.gingerninja.authenticator.core.codegen.TimeProvider
import me.gingerninja.authenticator.core.codegen.rememberCodeGeneratorState
import me.gingerninja.authenticator.core.codegen.rememberCodeState
import me.gingerninja.authenticator.core.preview.PreviewContainerWrapper
import me.gingerninja.authenticator.core.model.Account
import me.gingerninja.authenticator.core.model.Label
import me.gingerninja.authenticator.core.model.TotpAccount
import kotlin.time.Instant

@Composable
internal fun AccountCard(
    account: Account,
    codeGeneratorState: CodeGeneratorState,
    onClick: () -> Unit,
    onCopyClick: (code: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val codeState = rememberCodeState(codeGeneratorState, account)
    val code = codeState.code

    Card(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        //modifier = Modifier.padding(horizontal = 16.dp),
                        text = account.title ?: account.accountName,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    AccountCodeField(
                        //modifier = Modifier.padding(horizontal = 16.dp),
                        code = code,
                        onClick = {
                            onCopyClick(code)
                        },
                    )
                }

                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        progress = { codeState.remainingTimeFraction },
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = .25f),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }


            if (account.labels.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                    ),
                ) {
                    items(account.labels.toList()) { label ->
                        val color = Color(label.color)
                        val labelColor = if (color.luminance() < .5) {
                            Color.White
                        } else {
                            Color.Black
                        }

                        CompositionLocalProvider(LocalContentColor provides labelColor) {
                            Surface(
                                shape = CircleShape,
                                color = color,
                                contentColor = labelColor,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = label.name,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountCodeField(
    code: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pause = remember(code.length) {
        when (code.length) {
            4 -> intArrayOf(2)
            5 -> intArrayOf(2, 3)
            6 -> intArrayOf(3)
            7 -> intArrayOf(3, 4)
            8 -> intArrayOf(4)
            else -> intArrayOf()
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val size = with(LocalDensity.current) {
            (1.3 * 20).sp.toDp()
        }

        code.forEachIndexed { index, ch ->
            if (pause.contains(index)) {
                Spacer(modifier = Modifier.width(0.dp))
            }

            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(4.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = ch,
                    transitionSpec = {
                        fadeIn() + slideInVertically { -it } togetherWith fadeOut() + slideOutVertically { it }
                    }
                ) { ch ->
                    Text(
                        text = "$ch",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both,
                                mode = LineHeightStyle.Mode.Tight,
                            ),
                            fontSize = 20.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(PreviewContainerWrapper::class)
@Composable
private fun AccountCardPreview() {
    AccountCard(
        account = TotpAccount(
            id = 2,
            uid = "98765",
            accountName = "totp@test",
            title = "TOTP test",
            issuer = "test",
            period = 30,
            secret = "HSKN2IACERBAAU6LLETC6RFJL7LZOUY3XW5ASF4M5TEHBCJQNE577JP3MMTXVP4B27OK2TURZIFNQ36GQGH4YPSAKR3HER6WOR2JWFQ",
            digits = 6,
            source = Account.Source.MANUAL,
            algorithm = Account.Algorithm.SHA256,
            createdAt = Instant.fromEpochSeconds(0),
            updatedAt = Instant.fromEpochSeconds(0),
            labels = setOf(
                Label(
                    id = 0,
                    uid = "",
                    name = "My label",
                    color = 0xffff9900,
                    createdAt = Instant.fromEpochSeconds(20),
                    updatedAt = Instant.fromEpochSeconds(20),
                    icon = null,
                    position = 0,
                    numberOfAccounts = 1,
                )
            )
        ),
        codeGeneratorState = rememberCodeGeneratorState(
            OtpGenerator(
                object : TimeProvider {
                    override fun getCurrentTime() = Instant.fromEpochSeconds(10)
                }
            )
        ),
        onClick = {},
        onCopyClick = {},
    )
}