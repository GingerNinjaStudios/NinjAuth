package me.gingerninja.authenticator.core.codegen

import kotlinx.datetime.Instant
import me.gingerninja.authenticator.core.codegen.OtpGenerator
import me.gingerninja.authenticator.core.codegen.TimeProvider
import me.gingerninja.authenticator.core.model.Account
import me.gingerninja.authenticator.core.model.HotpAccount
import me.gingerninja.authenticator.core.model.TotpAccount
import org.junit.Assert.assertEquals
import org.junit.Test

class OtpGeneratorTest {
    private val fixedTimeProvider = object : TimeProvider {
        override fun getCurrentTime() = Instant.fromEpochSeconds(10)
    }

    private val otpGenerator = OtpGenerator(fixedTimeProvider)

    @Test
    fun getCodeHotp() {
        val account = HotpAccount(
            id = 1,
            uid = "12345",
            accountName = "hotp@test",
            title = "HOTP test",
            issuer = "test",
            counter = 0,
            secret = "HSKN2IACERBAAU6LLETC6RFJL7LZOUY3XW5ASF4M5TEHBCJQNE577JP3MMTXVP4B27OK2TURZIFNQ36GQGH4YPSAKR3HER6WOR2JWFQ",
            digits = 6,
            source = Account.Source.MANUAL,
            algorithm = Account.Algorithm.SHA1
        )

        assertEquals("269990", otpGenerator.getCode(account))
    }

    @Test
    fun getCodeTotp() {
        val account = TotpAccount(
            id = 2,
            uid = "98765",
            accountName = "totp@test",
            title = "TOTP test",
            issuer = "test",
            period = 30,
            secret = "HSKN2IACERBAAU6LLETC6RFJL7LZOUY3XW5ASF4M5TEHBCJQNE577JP3MMTXVP4B27OK2TURZIFNQ36GQGH4YPSAKR3HER6WOR2JWFQ",
            digits = 6,
            source = Account.Source.MANUAL,
            algorithm = Account.Algorithm.SHA256
        )

        assertEquals("418204", otpGenerator.getCode(account))
    }

    @Test
    fun getRemainingTime() {
        val account = TotpAccount(
            id = 2,
            uid = "98765",
            accountName = "totp@test",
            title = "TOTP test",
            issuer = "test",
            period = 30,
            secret = "HSKN2IACERBAAU6LLETC6RFJL7LZOUY3XW5ASF4M5TEHBCJQNE577JP3MMTXVP4B27OK2TURZIFNQ36GQGH4YPSAKR3HER6WOR2JWFQ",
            digits = 6,
            source = Account.Source.MANUAL,
            algorithm = Account.Algorithm.SHA256
        )

        val remaining = otpGenerator.getRemainingTime(account)

        assertEquals(20, remaining.inWholeSeconds)
    }
}