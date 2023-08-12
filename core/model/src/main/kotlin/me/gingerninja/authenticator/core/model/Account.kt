package me.gingerninja.authenticator.core.model

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

sealed interface Account {
    val id: Long
    val uid: String
    val accountName: String
    val secret: String
    val digits: Int
    val source: Source
    val algorithm: Algorithm
    val labels: ImmutableSet<Label>
    val title: String?
    val issuer: String?
    val position: Int

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
data class TotpAccount(
    override val id: Long,
    override val uid: String,
    override val accountName: String,
    val period: Long,
    override val secret: String,
    override val digits: Int,
    override val source: Account.Source,
    override val algorithm: Account.Algorithm,
    override val labels: ImmutableSet<Label> = persistentSetOf(),
    override val title: String? = null,
    override val issuer: String? = null,
    override val position: Int = -1
) : Account

/**
 * HMAC-Based One-Time Password (HOTP) account
 */
data class HotpAccount(
    override val id: Long,
    override val uid: String,
    override val accountName: String,
    val counter: Long,
    override val secret: String,
    override val digits: Int,
    override val source: Account.Source,
    override val algorithm: Account.Algorithm,
    override val labels: ImmutableSet<Label> = persistentSetOf(),
    override val title: String? = null,
    override val issuer: String? = null,
    override val position: Int = -1
) : Account