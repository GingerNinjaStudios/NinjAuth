package me.gingerninja.authenticator.codegen

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class OtpGenerator {
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

    companion object {
        private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
        private val BASE32_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray()

        private val BASE32_CHAR_MAP: Map<Char, Int> = buildMap {
            BASE32_ARRAY.forEachIndexed { index, c ->
                put(c, index)
            }
        }

        private val DIGITS_POWER =
            intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000) // from 0 to 8
    }
}