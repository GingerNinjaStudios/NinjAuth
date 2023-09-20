package me.gingerninja.authenticator.core.auth.biometric

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.MainThread
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

internal class BiometricKeyHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: NinjAuthSettings,
) {
    private val biometricManager by lazy {
        BiometricManager.from(context)
    }

    private val keyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    }

    val status: Status
        get() = biometricManager.canAuthenticate(AUTHENTICATORS).asStatus

    @MainThread
    fun authenticate(activity: FragmentActivity, cipher: Cipher): CompletableDeferred<Cipher> {
        val completable = CompletableDeferred<Cipher>()

        val prompt = BiometricPrompt(activity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                completable.completeExceptionally(
                    BiometricException(
                        code = errorCode,
                        message = errString.toString()
                    )
                )
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                result.cryptoObject?.cipher?.also {
                    completable.complete(it)
                } ?: run {
                    completable.completeExceptionally(NoCipherException())
                }
            }
        })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        prompt.authenticate(info, CryptoObject(cipher))

        return completable
    }

    @Throws(
        NoSuchProviderException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class
    )
    fun createKey(): SecretKey? {
        val specBuilder = KeyGenParameterSpec.Builder(
            KEY_ALIAS_BIOMETRIC,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        val keyGenerator: KeyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        ).apply { init(specBuilder.build()) }

        return keyGenerator.generateKey()
    }

    suspend fun remove(){
        settings.disableBiometrics()
        deleteKey()
    }

    private fun deleteKey() {
        keyStore.deleteEntry(KEY_ALIAS_BIOMETRIC)
        keyStore.load(null)
    }

    @Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        UnrecoverableKeyException::class
    )
    fun getKey(): SecretKey = keyStore.getKey(KEY_ALIAS_BIOMETRIC, null) as SecretKey

    enum class Status {
        /**
         * The user can successfully authenticate.
         */
        AVAILABLE,

        /**
         * The user can't authenticate because no biometric is enrolled.
         */
        NO_BIOMETRICS,

        /**
         * The user can't authenticate because the specified options are incompatible with the
         * current Android version.
         */
        UNSUPPORTED,

        /**
         * The user can't authenticate because the hardware is unavailable. Try again later.
         */
        UNAVAILABLE_HARDWARE_BUSY,

        /**
         * The user can't authenticate because there is no suitable hardware (e.g. no biometric
         * sensor).
         */
        UNAVAILABLE_NO_HARDWARE,

        /**
         * The user can't authenticate because a security vulnerability has been discovered with one
         * or more hardware sensors. The affected sensor(s) are unavailable until a security update
         * has addressed the issue.
         */
        UPDATE_REQUIRED,

        /**
         * Unable to determine whether the user can authenticate.
         *
         * This status code may be returned on older Android versions due to partial incompatibility
         * with a newer API. Applications that wish to enable biometric authentication on affected
         * devices may still call [BiometricPrompt.authenticate] after receiving this status
         * code but should be prepared to handle possible errors.
         */
        UNKNOWN
    }

    companion object {
        private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG/* or
                BiometricManager.Authenticators.BIOMETRIC_WEAK*/

        private const val KEY_ALIAS_BIOMETRIC = "NABiometric"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    }
}

/**
 * Exception thrown when [BiometricPrompt.AuthenticationCallback.onAuthenticationError] happens.
 *
 * @param code the error code
 * @param message the error message
 *
 * @see [BiometricPrompt.AuthenticationError]
 */
class BiometricException(val code: Int, override val message: String?) : RuntimeException(message) {
    companion object {
        const val ERROR_KEY_INVALIDATED = -1
        const val ERROR_SHOULD_RETRY = -2
        const val ERROR_KEY_SECURITY_UPDATE = -3
    }
}

/**
 * Exception that can occur when [BiometricPrompt.AuthenticationCallback.onAuthenticationSucceeded]
 * seemingly authenticates the user correctly but the result object has no [Cipher] that could be
 * used for key operations.
 */
class NoCipherException : RuntimeException()

private val Int.asStatus: BiometricKeyHandler.Status
    get() = when (this) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricKeyHandler.Status.AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricKeyHandler.Status.UNSUPPORTED
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricKeyHandler.Status.UNAVAILABLE_HARDWARE_BUSY
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricKeyHandler.Status.NO_BIOMETRICS
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricKeyHandler.Status.UNAVAILABLE_NO_HARDWARE
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricKeyHandler.Status.UPDATE_REQUIRED
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricKeyHandler.Status.UNKNOWN
        else -> BiometricKeyHandler.Status.UNKNOWN
    }