package me.gingerninja.authenticator.crypto;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.auth.DestroyFailedException;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.SingleSubject;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;
import timber.log.Timber;

@Singleton
public class Crypto {
    public static final String PROTECTION_MODE_NONE = "none";
    public static final String PROTECTION_MODE_PIN = "pin";
    public static final String PROTECTION_MODE_PASSWORD = "password";
    public static final String PROTECTION_MODE_BIO_PIN = "bio_pin";
    public static final String PROTECTION_MODE_BIO_PASSWORD = "bio_password";
    public static final int AUTH_MODE_PASSWORD = 0;
    public static final int AUTH_MODE_BIOMETRIC = 1;
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS_BIOMETRIC = "biometric";
    private static final String KEY_MASTER_PASS = "key_pass";
    private static final String KEY_MASTER_BIO = "key_bio";
    @NonNull
    private final Context context;
    private final KeyguardManager keyguardManager;
    @NonNull
    private final DatabaseHandler dbHandler;
    @NonNull
    private final SharedPreferences sharedPrefs;
    private final Features features;
    private SecureRandom secureRandom = new SecureRandom();
    private KeyStore keyStore;
    @ProtectionMode
    private String protectionMode;

    @Inject
    Crypto(@NonNull Context context, @NonNull DatabaseHandler dbHandler, @NonNull SharedPreferences sharedPrefs) {
        this.context = context.getApplicationContext();
        this.dbHandler = dbHandler;
        this.sharedPrefs = sharedPrefs;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager = context.getSystemService(KeyguardManager.class);
        } else {
            keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }

        String sharedPrefsProtKey = context.getString(R.string.settings_protection_key);
        protectionMode = sharedPrefs.getString(sharedPrefsProtKey, null);

        features = new Features();

        try {
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Features getFeatures() {
        return features;
    }

    public Completable test(FragmentActivity fragmentActivity) {
        return Completable.fromAction(() -> {
            testImpl(fragmentActivity);
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void testImpl(FragmentActivity fragmentActivity) {
        try {
            Timber.v("Creating keys");
            create(fragmentActivity, "fake".toCharArray(), true).blockingAwait();
        } catch (Throwable t) {
            Timber.e(t, "Crypto error: %s", t.getMessage());
        } finally {
            try {
                keyStore.deleteEntry(KEY_ALIAS_BIOMETRIC);
            } catch (KeyStoreException e) {
                Timber.e(e, "Cannot delete biometric key: %s", e.getMessage());
            }

            sharedPrefs.edit().remove(KEY_MASTER_BIO).remove(KEY_MASTER_PASS).commit();
        }
    }

    private void openDatabase(SecretKey secretKey) {
        String encrypted = sharedPrefs.getString("", null);
    }

    private boolean isDeviceSecure() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return keyguardManager.isDeviceSecure();
        } else {
            return keyguardManager.isKeyguardSecure();
        }
    }

    public void update(@NonNull ChangeRequest changeRequest) {
        if (changeRequest.isChangingProtectionMode(protectionMode)) {

        }
    }

    public void authenticate(@NonNull FragmentActivity activity) {
        authenticate(activity, AUTH_MODE_BIOMETRIC);
    }

    // TODO how to store auto-backup password securely in memory
    /*
    Initial setup:
        1. create RSA key in AndroidKeyStore with setEncryptionRequired()

    Usage:
        1. read & decrypt the auto-backup password
        2. get RSA key from AndroidKeyStore
        3. encrypt the auto-backup password using RSA and store those bytes anywhere
            a. if exception is thrown at the cipher, the RSA key must be regenerated first
        4. when the password is needed, use the RSA key to decrypt the data into a char array
     */

    public void authenticate(@NonNull FragmentActivity activity, @AuthMode int mode) {
        switch (mode) {
            case AUTH_MODE_BIOMETRIC:
                authenticateBiometric(activity, null); // TODO
                break;
            case AUTH_MODE_PASSWORD:
                authenticatePassword(activity);
                break;
        }
    }

    private void authenticatePassword(@NonNull FragmentActivity activity) {
        // TODO
    }

    private Single<BiometricPrompt.CryptoObject> authenticateBiometric(@NonNull FragmentActivity activity, @NonNull Cipher cipher) {
        SingleSubject<BiometricPrompt.CryptoObject> subject = SingleSubject.create();

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Auth user")
                .setNegativeButtonText("Use password")
                .build();

        BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);

        BiometricPrompt prompt = new BiometricPrompt(activity, Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Timber.d("[AUTH] error: %d, str: %s", errorCode, errString);
                switch (errorCode) {
                    case BiometricConstants.ERROR_HW_NOT_PRESENT:
                    case BiometricConstants.ERROR_NEGATIVE_BUTTON:
                        authenticatePassword(activity);
                        break;
                    case BiometricConstants.ERROR_LOCKOUT:
                        // TODO Too many attempts. Try again later.
                        break;
                }

                subject.onError(new BiometricException(errorCode, errString));
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Timber.d("[AUTH] success: %s, crypto: %s", result, result.getCryptoObject());

                //noinspection ConstantConditions
                subject.onSuccess(result.getCryptoObject());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Timber.d("[AUTH] failed");
                subject.onError(new AuthenticationFailedException());
            }
        });

        Single.just(prompt)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(biometricPrompt -> biometricPrompt.authenticate(promptInfo, cryptoObject));


        return subject.hide();
    }

    private Completable create(FragmentActivity fragmentActivity, char[] password, boolean useBiometrics) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
        CompletableSubject subject = CompletableSubject.create();

        SecretKey master = generateMasterKey();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);

        SecretKey passwordKey = generatePasswordKey(password, salt);
        Cipher passwordCipher = getCipher(Cipher.WRAP_MODE, passwordKey, false);
        String passwordWrappedKey = Base64.encodeToString(salt, Base64.NO_PADDING | Base64.NO_WRAP) + '.' + wrapKeyAndEncode(master, passwordCipher);

        try {
            passwordKey.destroy();
        } catch (DestroyFailedException e) {
            Timber.e(e, "Cannot destroy password key: %s", e.getMessage());
        }

        // save passwordWrappedKey
        sharedPrefs.edit().putString(KEY_MASTER_PASS, passwordWrappedKey).apply();

        if (useBiometrics && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SecretKey biometricKey = generateBiometricKeyApi23();
            Cipher biometricCipher = getCipher(Cipher.WRAP_MODE, biometricKey, true);

            authenticateBiometric(fragmentActivity, biometricCipher)
                    .doAfterTerminate(() -> {
                        try {
                            biometricKey.destroy();
                        } catch (Throwable e) {
                            Timber.e(e, "Cannot destroy biometric key: %s", e.getMessage());
                        }
                        subject.onComplete();
                    })
                    .subscribe(cryptoObject -> {
                        String biometricWrappedKey = wrapKeyAndEncode(master, cryptoObject.getCipher());
                        // save the biometricWrappedKey
                        sharedPrefs.edit().putString(KEY_MASTER_BIO, biometricWrappedKey).apply();
                    }, throwable -> {
                        Timber.e(throwable, "Failed to wrap and encode master key with biometric: %s", throwable.getMessage());
                    });
        } else {
            subject.onComplete();
        }

        return subject;
    }

    private String encrypt(Cipher cipher, char[] chars) throws IllegalBlockSizeException, BadPaddingException {
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());

        return encrypt(cipher, bytes);
    }

    private String encrypt(Cipher cipher, byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
        byte[] iv = cipher.getIV(); // needed for GCM as Android may change the IV

        byte[] encryptedRaw = cipher.doFinal(bytes);

        Arrays.fill(bytes, (byte) 0);

        byte[] results = new byte[1 + iv.length + encryptedRaw.length]; // IV-length + IV + wrapped key
        results[0] = (byte) iv.length;
        System.arraycopy(iv, 0, results, 1, iv.length);
        System.arraycopy(encryptedRaw, 0, results, 1 + iv.length, encryptedRaw.length);

        Arrays.fill(iv, (byte) 0);

        return Base64.encodeToString(results, Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private char[] decryptToChars(Cipher cipher, String encoded) throws IllegalBlockSizeException, BadPaddingException {
        byte[] decrypted = decrypt(cipher, encoded);

        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(decrypted));
        char[] data = Arrays.copyOf(charBuffer.array(), charBuffer.limit());

        Arrays.fill(decrypted, (byte) 0);

        return data;
    }

    private byte[] decrypt(Cipher cipher, String encoded) throws BadPaddingException, IllegalBlockSizeException {
        byte[] data = Base64.decode(encoded, Base64.NO_PADDING | Base64.NO_WRAP);

        int ivLength = data[0];

        byte[] encrypted = new byte[data.length - ivLength - 1];
        System.arraycopy(data, 1 + ivLength, encrypted, 0, encrypted.length);

        return cipher.doFinal(encrypted);
    }

    private String wrapKeyAndEncode(SecretKey keyToWrap, Cipher cipher) throws IllegalBlockSizeException, InvalidKeyException {
        return Base64.encodeToString(wrapKey(keyToWrap, cipher), Base64.NO_PADDING | Base64.NO_WRAP);
    }

    /**
     * @param keyToWrap
     * @param cipher
     * @return a byte array containing the length of the IV (the first byte), the IV itself, and the
     * wrapped key
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     */
    private byte[] wrapKey(SecretKey keyToWrap, Cipher cipher) throws InvalidKeyException, IllegalBlockSizeException {
        byte[] iv = cipher.getIV(); // needed for GCM as Android may change the IV

        byte[] wrappedKey = cipher.wrap(keyToWrap);

        byte[] results = new byte[1 + iv.length + wrappedKey.length]; // IV-length + IV + wrapped key
        results[0] = (byte) iv.length;
        System.arraycopy(iv, 0, results, 1, iv.length);
        System.arraycopy(wrappedKey, 0, results, 1 + iv.length, wrappedKey.length);

        return results;
    }

    private SecretKey unwrapKey(String data, Cipher cipher) throws NoSuchAlgorithmException, InvalidKeyException {
        return unwrapKey(Base64.decode(data, Base64.NO_PADDING | Base64.NO_WRAP), cipher);
    }

    private SecretKey unwrapKey(byte[] data, Cipher cipher) throws NoSuchAlgorithmException, InvalidKeyException {
        int ivLength = data[0];
        byte[] wrappedKey = new byte[data.length - ivLength - 1];
        System.arraycopy(data, 1 + ivLength, wrappedKey, 0, wrappedKey.length);

        return (SecretKey) cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
    }

    /**
     * Gets the cipher for encrypting / wrapping purposes.
     *
     * @param purpose
     * @param secretKey
     * @param systemGeneratesIV
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    private Cipher getCipher(@CipherPurposeWrite int purpose, Key secretKey, boolean systemGeneratesIV) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        GCMParameterSpec parameterSpec = null;

        if (!systemGeneratesIV) {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);
            parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        }

        // creating the cipher
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(purpose, secretKey, parameterSpec);
        //GCMParameterSpec gcmspec = cipher.getParameters().getParameterSpec(GCMParameterSpec.class);
        return cipher;
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
    private Cipher getCipher(@CipherPurposeRead int purpose, Key secretKey, String encoded) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        return getCipher(purpose, secretKey, Base64.decode(encoded, Base64.NO_PADDING | Base64.NO_WRAP));
    }

    private Cipher getCipher(@CipherPurposeRead int purpose, Key secretKey, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        int ivLength = data[0];
        byte[] iv = new byte[ivLength];
        System.arraycopy(data, 0, iv, 0, ivLength);

        byte[] encrypted = new byte[data.length - iv.length - 1];
        System.arraycopy(data, 1 + ivLength, encrypted, 0, encrypted.length);

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        cipher.init(purpose, secretKey, parameterSpec);

        return cipher;
    }

    private SecretKey generateMasterKey() {
        // creating AES
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        return new SecretKeySpec(key, "AES");
    }

    private SecretKey generatePasswordKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); // TODO PBKDF2WithHmacSHA1 on pre-26
        PBEKeySpec spec = new PBEKeySpec(password, salt, 100000, 256); //iterationCount: 65536
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        spec.clearPassword();

        return secret;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey generateBiometricKeyApi23() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenParameterSpec.Builder specBuilder = new KeyGenParameterSpec.Builder(KEY_ALIAS_BIOMETRIC, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setUserAuthenticationRequired(true)
                .setKeySize(128)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            specBuilder.setInvalidatedByBiometricEnrollment(false);
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
        keyGenerator.init(specBuilder.build());

        return keyGenerator.generateKey();
    }

    @StringDef({PROTECTION_MODE_NONE, PROTECTION_MODE_PIN, PROTECTION_MODE_PASSWORD, PROTECTION_MODE_BIO_PIN, PROTECTION_MODE_BIO_PASSWORD})
    @interface ProtectionMode {
    }

    /*private void createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        // creating AES
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        // creating IV for AES
        byte[] iv = new byte[12]; //NEVER REUSE THIS IV WITH SAME KEY
        secureRandom.nextBytes(iv);

        // creating the cipher
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
    }*/

    @IntDef({Cipher.ENCRYPT_MODE, Cipher.WRAP_MODE})
    @interface CipherPurposeWrite {
    }

    @IntDef({Cipher.DECRYPT_MODE, Cipher.UNWRAP_MODE})
    @interface CipherPurposeRead {
    }

    @IntDef({AUTH_MODE_PASSWORD, AUTH_MODE_BIOMETRIC})
    @interface AuthMode {
    }

    public static class ChangeRequest {
        @Nullable
        private final char[] oldPassword;

        @Nullable
        private char[] newPassword;

        @Nullable
        @ProtectionMode
        private String protectionMode;

        public ChangeRequest(@Nullable char[] oldPassword) {
            this.oldPassword = oldPassword;
        }

        @Nullable
        public char[] getOldPassword() {
            return oldPassword;
        }

        @Nullable
        public char[] getNewPassword() {
            return newPassword;
        }

        public ChangeRequest setNewPassword(@Nullable char[] newPassword) {
            this.newPassword = newPassword;
            return this;
        }

        @Nullable
        public String getProtectionMode() {
            return protectionMode;
        }

        public ChangeRequest setProtectionMode(String protectionMode) {
            this.protectionMode = protectionMode;
            return this;
        }

        boolean isChangingProtectionMode(@ProtectionMode String oldProtectionMode) {
            return !TextUtils.equals(oldProtectionMode, protectionMode);
        }

        boolean isChangingPassword() {
            if (newPassword == null) {
                return false;
            }

            return !Arrays.equals(oldPassword, newPassword);
        }
    }

    public class Features {
        public static final int BIO_OK = 0;
        public static final int BIO_DEVICE_NOT_SECURE = 1;
        public static final int BIO_NO_FINGERPRINTS = 2;
        public static final int BIO_NOT_AVAILABLE = 3;

        public boolean hasBiometrics() {
            return getBiometricsResults() == BIO_OK;
        }

        @SuppressWarnings("deprecation")
        public int getBiometricsResults() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return BIO_NOT_AVAILABLE;
            } else if (!isDeviceSecure()) {
                return BIO_DEVICE_NOT_SECURE;
            } else if (!FingerprintManagerCompat.from(context).hasEnrolledFingerprints()) {
                return BIO_NO_FINGERPRINTS;
            } else {
                return BIO_OK;
            }
        }

        public String[] getFeatureValues() {
            if (hasBiometrics()) {
                return context.getResources().getStringArray(R.array.settings_protection_values);
            } else {
                return context.getResources().getStringArray(R.array.settings_protection_values_no_bio);
            }
        }

        public String[] getFeatureEntries() {
            if (hasBiometrics()) {
                return context.getResources().getStringArray(R.array.settings_protection_entries);
            } else {
                return context.getResources().getStringArray(R.array.settings_protection_entries_no_bio);
            }
        }
    }
}
