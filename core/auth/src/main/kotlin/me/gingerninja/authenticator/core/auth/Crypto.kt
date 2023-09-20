package me.gingerninja.authenticator.core.auth

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class Crypto {
    private val secureRandom = SecureRandom()

    /**
     * Generates an AES key.
     */
    fun generateKey(): SecretKey {
        val key = ByteArray(32).apply {
            secureRandom.nextBytes(this)
        }

        return SecretKeySpec(key, "AES")
    }

    fun generateDbPass(): CharArray {
        val bytes = ByteArray(24)
        secureRandom.nextBytes(bytes)

        val encoded = Base64.encode(bytes, Base64.NO_PADDING or Base64.NO_WRAP)
        bytes.fill(0)

        val charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded))
        val data = charBuffer.array().copyOf(charBuffer.limit())

        encoded.fill(0)
        return data
    }

    @Throws(IllegalBlockSizeException::class, BadPaddingException::class)
    fun encrypt(cipher: Cipher, chars: CharArray): String {
        val byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars))
        val bytes = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit())

        return encrypt(cipher, bytes)
    }

    @Throws(BadPaddingException::class, IllegalBlockSizeException::class)
    fun encrypt(cipher: Cipher, bytes: ByteArray): String {
        val iv = cipher.iv // needed for GCM as Android may change the IV
        val encryptedRaw = cipher.doFinal(bytes)

        // clear the original byte array
        bytes.fill(0)

        val results = ByteArray(1 + iv.size + encryptedRaw.size) // IV-length + IV + wrapped key
        results[0] = iv.size.toByte()

        System.arraycopy(iv, 0, results, 1, iv.size)
        System.arraycopy(encryptedRaw, 0, results, 1 + iv.size, encryptedRaw.size)

        // clear the IV
        iv.fill(0)

        return Base64.encodeToString(results, Base64.NO_PADDING or Base64.NO_WRAP)
    }

    @Throws(IllegalBlockSizeException::class, BadPaddingException::class)
    fun decryptToChars(cipher: Cipher, encoded: String): CharArray {
        val decrypted = decrypt(cipher, encoded)
        val charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(decrypted))
        val data = Arrays.copyOf(charBuffer.array(), charBuffer.limit())

        decrypted.fill(0)

        return data
    }

    @Throws(BadPaddingException::class, IllegalBlockSizeException::class)
    fun decrypt(cipher: Cipher, encoded: String): ByteArray {
        val data = Base64.decode(encoded, Base64.NO_PADDING or Base64.NO_WRAP)
        val ivLength = data[0].toInt()
        val encrypted = ByteArray(data.size - ivLength - 1)

        System.arraycopy(data, 1 + ivLength, encrypted, 0, encrypted.size)

        return cipher.doFinal(encrypted)
    }

    @Throws(IllegalBlockSizeException::class, InvalidKeyException::class)
    fun wrapKeyAndEncode(keyToWrap: SecretKey, cipher: Cipher): String {
        return Base64.encodeToString(
            wrapKey(keyToWrap, cipher),
            Base64.NO_PADDING or Base64.NO_WRAP
        )
    }

    /**
     * @param keyToWrap
     * @param cipher
     * @return a byte array containing the length of the IV (the first byte), the IV itself, and the
     * wrapped key
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     */
    @Throws(InvalidKeyException::class, IllegalBlockSizeException::class)
    fun wrapKey(keyToWrap: SecretKey, cipher: Cipher): ByteArray {
        val iv = cipher.iv // needed for GCM as Android may change the IV
        val wrappedKey = cipher.wrap(keyToWrap)
        val results = ByteArray(1 + iv.size + wrappedKey.size) // IV-length + IV + wrapped key
        results[0] = iv.size.toByte()
        System.arraycopy(iv, 0, results, 1, iv.size)
        System.arraycopy(wrappedKey, 0, results, 1 + iv.size, wrappedKey.size)
        return results
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun unwrapKey(data: String, cipher: Cipher): SecretKey {
        return unwrapKey(Base64.decode(data, Base64.NO_PADDING or Base64.NO_WRAP), cipher)
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun unwrapKey(data: ByteArray, cipher: Cipher): SecretKey {
        val ivLength = data[0].toInt()
        val wrappedKey = ByteArray(data.size - ivLength - 1)

        System.arraycopy(data, 1 + ivLength, wrappedKey, 0, wrappedKey.size)

        return cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY) as SecretKey
    }

    /**
     * Gets the cipher for encrypting / wrapping purposes.
     *
     * @param purpose
     * @param secretKey
     * @param systemGeneratesIV whether the IV is generated by the encryption operation later
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class
    )
    fun getCipher(
        purpose: CipherWrite,
        secretKey: Key,
        systemGeneratesIV: Boolean
    ): Cipher {
        val parameterSpec = if (!systemGeneratesIV) {
            val iv = ByteArray(12)
            secureRandom.nextBytes(iv)
            GCMParameterSpec(128, iv) //128 bit auth tag length
        } else {
            null
        }

        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(purpose.mode, secretKey, parameterSpec)
        }
    }

    /**
     * Gets the cipher for decrypting / unwrapping purposes.
     *
     * @param purpose
     * @param secretKey
     * @param encoded
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class
    )
    fun getCipher(
        purpose: CipherRead,
        secretKey: Key,
        encoded: String
    ): Cipher {
        return getCipher(
            purpose = purpose,
            secretKey = secretKey,
            data = Base64.decode(encoded, Base64.NO_PADDING or Base64.NO_WRAP)
        )
    }

    /**
     * Gets the cipher for decrypting / unwrapping purposes.
     *
     * @param purpose
     * @param secretKey
     * @param data
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class
    )
    fun getCipher(
        purpose: CipherRead,
        secretKey: Key,
        data: ByteArray
    ): Cipher {
        val ivLength = data[0].toInt()
        val iv = ByteArray(ivLength)
        System.arraycopy(data, 1, iv, 0, ivLength)

        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            val parameterSpec = GCMParameterSpec(128, iv) // 128 bit auth tag length
            init(purpose.mode, secretKey, parameterSpec)
        }
    }

    enum class CipherWrite(val mode: Int) {
        ENCRYPT(Cipher.ENCRYPT_MODE),
        WRAP(Cipher.WRAP_MODE)
    }

    enum class CipherRead(val mode: Int) {
        DECRYPT(Cipher.DECRYPT_MODE),
        UNWRAP(Cipher.UNWRAP_MODE)
    }
}