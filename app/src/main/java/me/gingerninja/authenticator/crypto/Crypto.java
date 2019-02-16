package me.gingerninja.authenticator.crypto;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;
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

    @StringDef({PROTECTION_MODE_NONE, PROTECTION_MODE_PIN, PROTECTION_MODE_PASSWORD, PROTECTION_MODE_BIO_PIN, PROTECTION_MODE_BIO_PASSWORD})
    @interface ProtectionMode {
    }

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS_BIOMETRIC = "biometric";

    private static final String KEY_MASTER_PASS = "key_pass";
    private static final String KEY_MASTER_BIO = "key_bio";

    public static final int AUTH_MODE_PASSWORD = 0;
    public static final int AUTH_MODE_BIOMETRIC = 1;

    @IntDef({AUTH_MODE_PASSWORD, AUTH_MODE_BIOMETRIC})
    @interface AuthMode {
    }

    @NonNull
    private final DatabaseHandler dbHandler;

    @NonNull
    private final SharedPreferences sharedPrefs;

    private SecureRandom secureRandom = new SecureRandom();
    private KeyStore keyStore;

    @ProtectionMode
    private String protectionMode;

    @Inject
    Crypto(Context context, @NonNull DatabaseHandler dbHandler, @NonNull SharedPreferences sharedPrefs) {
        this.dbHandler = dbHandler;
        this.sharedPrefs = sharedPrefs;

        String sharedPrefsProtKey = context.getString(R.string.settings_protection_key);
        protectionMode = sharedPrefs.getString(sharedPrefsProtKey, null);

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
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

    private void openDatabase(SecretKey secretKey) {
        String encrypted = sharedPrefs.getString("", null);
    }

    public void update(@NonNull ChangeRequest changeRequest) {
        if (changeRequest.isChangingProtectionMode(protectionMode)) {

        }
    }

    public void authenticate(@NonNull FragmentActivity activity) {
        authenticate(activity, AUTH_MODE_BIOMETRIC);
    }

    public void authenticate(@NonNull FragmentActivity activity, @AuthMode int mode) {
        switch (mode) {
            case AUTH_MODE_BIOMETRIC:
                authenticateBiometric(activity);
                break;
            case AUTH_MODE_PASSWORD:
                authenticatePassword(activity);
                break;
        }
    }

    private void authenticatePassword(@NonNull FragmentActivity activity) {
        // TODO
    }

    private void authenticateBiometric(@NonNull FragmentActivity activity) {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Auth user")
                .setNegativeButtonText("Use password")
                .build();

        BiometricPrompt.CryptoObject cryptoObject = null;// TODO new BiometricPrompt.CryptoObject();

        new BiometricPrompt(activity, Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {
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
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Timber.d("[AUTH] success: %s, crypto: %s", result, result.getCryptoObject());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Timber.d("[AUTH] failed");
            }
        })
                .authenticate(promptInfo);
    }

    private void create(char[] password, boolean useBiometrics) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
        SecretKey master = generateMasterKey();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);

        SecretKey passwordKey = generatePasswordKey(password, salt);
        String passwordWrappedKey = Base64.encodeToString(salt, Base64.NO_PADDING | Base64.NO_WRAP) + '.' + wrapKeyAndEncode(master, passwordKey);
        // TODO save passwordWrappedKey

        if (useBiometrics && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SecretKey biometricKey = generateBiometricKeyApi23();
            String biometricWrappedKey = wrapKeyAndEncode(master, biometricKey);
            // TODO save the biometricWrappedKey
        }
    }

    private String encrypt(SecretKey secretKey, char[] chars) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());

        return encrypt(secretKey, bytes);
    }

    private String encrypt(SecretKey secretKey, byte[] bytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);

        // creating the cipher
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        iv = cipher.getIV(); // needed for GCM as Android may change the IV

        byte[] encryptedRaw = cipher.doFinal(bytes);

        Arrays.fill(bytes, (byte) 0);

        byte[] results = new byte[1 + iv.length + encryptedRaw.length]; // IV-length + IV + wrapped key
        results[0] = (byte) iv.length;
        System.arraycopy(iv, 0, results, 1, iv.length);
        System.arraycopy(encryptedRaw, 0, results, 1 + iv.length, encryptedRaw.length);

        Arrays.fill(iv, (byte) 0);

        return Base64.encodeToString(results, Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private char[] decryptToChars(SecretKey secretKey, String encoded) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] decrypted = decrypt(secretKey, encoded);

        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(decrypted));
        char[] data = Arrays.copyOf(charBuffer.array(), charBuffer.limit());

        Arrays.fill(decrypted, (byte) 0);

        return data;
    }

    private byte[] decrypt(SecretKey secretKey, String encoded) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] data = Base64.decode(encoded, Base64.NO_PADDING | Base64.NO_WRAP);

        int ivLength = data[0];
        byte[] iv = new byte[ivLength];
        System.arraycopy(data, 0, iv, 0, ivLength);

        byte[] encrypted = new byte[data.length - iv.length - 1];
        System.arraycopy(data, 1 + ivLength, encrypted, 0, encrypted.length);

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        return cipher.doFinal(encrypted);
    }

    private Cipher getCipherForDecryption(SecretKey secretKey, String encoded) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] data = Base64.decode(encoded, Base64.NO_PADDING | Base64.NO_WRAP);

        int ivLength = data[0];
        byte[] iv = new byte[ivLength];
        System.arraycopy(data, 0, iv, 0, ivLength);

        byte[] encrypted = new byte[data.length - iv.length - 1];
        System.arraycopy(data, 1 + ivLength, encrypted, 0, encrypted.length);

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        return cipher;
    }

    private String wrapKeyAndEncode(SecretKey keyToWrap, SecretKey wrapper) throws IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return Base64.encodeToString(wrapKey(keyToWrap, wrapper), Base64.NO_PADDING | Base64.NO_WRAP);
    }

    /**
     * @param keyToWrap
     * @param wrapper
     * @return a byte array containing the length of the IV (the first byte), the IV itself, and the
     * wrapped key
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     */
    private byte[] wrapKey(SecretKey keyToWrap, SecretKey wrapper) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException {
        // creating IV for AES
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);

        // creating the cipher
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        cipher.init(Cipher.WRAP_MODE, wrapper, parameterSpec);

        iv = cipher.getIV(); // needed for GCM as Android may change the IV

        byte[] wrappedKey = cipher.wrap(keyToWrap);

        byte[] results = new byte[1 + iv.length + wrappedKey.length]; // IV-length + IV + wrapped key
        results[0] = (byte) iv.length;
        System.arraycopy(iv, 0, results, 1, iv.length);
        System.arraycopy(wrappedKey, 0, results, 1 + iv.length, wrappedKey.length);

        return results;
    }

    private SecretKey unwrapKey(String data, SecretKey wrapper) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        return unwrapKey(Base64.decode(data, Base64.NO_PADDING | Base64.NO_WRAP), wrapper);
    }

    private SecretKey unwrapKey(byte[] data, SecretKey wrapper) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        int ivLength = data[0];
        byte[] iv = new byte[ivLength];
        System.arraycopy(data, 0, iv, 0, ivLength);

        byte[] wrappedKey = new byte[data.length - iv.length - 1];
        System.arraycopy(data, 1 + ivLength, wrappedKey, 0, wrappedKey.length);

        // creating the cipher
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        cipher.init(Cipher.UNWRAP_MODE, wrapper, parameterSpec);
        return (SecretKey) cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
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
}
