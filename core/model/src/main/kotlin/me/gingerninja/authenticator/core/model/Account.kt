package me.gingerninja.authenticator.core.model

import javax.annotation.concurrent.Immutable
import kotlin.time.Instant

@Immutable
sealed interface Account {
    val id: Long
    val uid: String
    val accountName: String
    val secret: String
    val digits: Int
    val source: Source
    val algorithm: Algorithm
    val labels: Set<Label>
    val title: String?
    val issuer: String?
    val position: Int
    val createdAt: Instant
    val updatedAt: Instant

    enum class Source {
        URI, MANUAL
    }

    enum class Algorithm {
        SHA1, SHA256, SHA512
    }
}

/**
 * Time-Based One-Time Password (TOTP) account
 */
@Immutable
data class TotpAccount(
    override val id: Long,
    override val uid: String,
    override val accountName: String,
    val period: Long,
    override val secret: String,
    override val digits: Int,
    override val source: Account.Source,
    override val algorithm: Account.Algorithm,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val labels: Set<Label> = setOf(),
    override val title: String? = null,
    override val issuer: String? = null,
    override val position: Int = -1,
) : Account

/**
 * HMAC-Based One-Time Password (HOTP) account
 */
@Immutable
data class HotpAccount(
    override val id: Long,
    override val uid: String,
    override val accountName: String,
    val counter: Long,
    override val secret: String,
    override val digits: Int,
    override val source: Account.Source,
    override val algorithm: Account.Algorithm,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val labels: Set<Label> = setOf(),
    override val title: String? = null,
    override val issuer: String? = null,
    override val position: Int = -1,
) : Account