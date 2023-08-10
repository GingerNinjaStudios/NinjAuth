package me.gingerninja.authenticator.codegen

import androidx.annotation.IntRange
import me.gingerninja.authenticator.model.Account
import me.gingerninja.authenticator.model.HotpAccount
import me.gingerninja.authenticator.model.TotpAccount
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OtpGenerator(
    private val timeProvider: TimeProvider = DefaultTimeProvider
) {
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    private fun getRawHMAC(
        data: ByteArray,
        key: ByteArray,
        algorithm: OtpAlgorithm
    ): ByteArray {
        val signingKey = SecretKeySpec(key, algorithm.value)
        val mac = Mac.getInstance(algorithm.value).apply {
            init(signingKey)
        }

        return mac.doFinal(data)
    }

    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    private fun getHOTP(
        secret: String,
        data: Long,
        algorithm: OtpAlgorithm,
        @IntRange(from = 1, to = 8) digits: Int
    ): Int {
        val array = ByteArray(Long.SIZE_BYTES) {
            val shift = (Long.SIZE_BYTES - it - 1) * Byte.SIZE_BITS
            data.shr(shift).toByte()
        }
        return getHOTP(secret, array, algorithm, digits)
    }

    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    private fun getHOTP(
        secret: String,
        data: ByteArray,
        algorithm: OtpAlgorithm,
        @IntRange(from = 1, to = 8) digits: Int
    ): Int {
        require(digits in 1..8) { "Digits must be between 1 and 8" }
        val hash: ByteArray = getRawHMAC(
            data,
            Base32.decode(secret),
            algorithm
        )

        // DT
        val offset = hash[hash.size - 1].toInt() and 0xf
        val binary = hash[offset].toInt() and 0x7f shl 24 or
                (hash[offset + 1].toInt() and 0xff shl 16) or
                (hash[offset + 2].toInt() and 0xff shl 8) or
                (hash[offset + 3].toInt() and 0xff)

        // ST
        return binary % DIGITS_POWER[digits - 1]
    }

    /**
     * @param secret    the shared secret
     * @param period    the period in seconds
     * @param algorithm the algorithm to use
     * @param digits    number of digits to use between 0 and 8
     * @param steps     the extra period count (can be negative); default is 0
     * @return the OTP
     * @throws InvalidKeyException      if the generated key is invalid
     * @throws NoSuchAlgorithmException if the given crypto algorithm is non-existent
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    private fun getTOTP(
        secret: String,
        period: Long,
        algorithm: OtpAlgorithm,
        @IntRange(from = 1, to = 8) digits: Int,
        steps: Long = 0,
    ): Int {
        // T
        val counter = timeProvider.getCurrentTime().epochSeconds / period + steps

        val timeStr = counter.toHexString().uppercase(Locale.getDefault()).padStart(16, '0')
        return getHOTP(
            secret = secret,
            data = hexToBytes(timeStr),
            algorithm = algorithm,
            digits = digits
        )
    }

    /**
     * Gets the code of the passed [account].
     *
     * @param account the TOTP or HOTP account
     * @return the current authentication code
     * @see HotpAccount
     * @see TotpAccount
     */
    fun getCode(account: Account): String {
        val secret: String = account.secret
        val digits: Int = account.digits
        val algo: OtpAlgorithm = when (account.algorithm) {
            Account.Algorithm.SHA1 -> OtpAlgorithm.SHA1
            Account.Algorithm.SHA256 -> OtpAlgorithm.SHA256
            Account.Algorithm.SHA512 -> OtpAlgorithm.SHA512
        }

        val code = when (account) {
            is HotpAccount -> getHOTP(
                secret = secret,
                data = account.counter,
                algorithm = algo,
                digits = digits
            )

            is TotpAccount -> getTOTP(
                secret = secret,
                period = account.period,
                algorithm = algo,
                digits = digits
            )
        }

        return code.toString().padStart(digits, '0')
    }

    /**
     * Returns the remaining duration of the current period of the TOTP [account].
     *
     * @param account the [TotpAccount] account instance
     * @return the remaining duration of the current period
     * @see TotpAccount
     */
    fun getRemainingTime(account: TotpAccount): Duration {
        val period = account.period.coerceAtLeast(1).seconds

        val time = timeProvider.getCurrentTime().toEpochMilliseconds().milliseconds

        val fracturedTime = period * floor(time / period)

        return period - (time - fracturedTime)
    }

    companion object {
        private val DIGITS_POWER =
            intArrayOf(10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000) // from 1 to 8
    }
}