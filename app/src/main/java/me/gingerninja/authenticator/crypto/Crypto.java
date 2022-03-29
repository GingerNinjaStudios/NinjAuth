package me.gingerninja.authenticator.crypto;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.annotation.StringDef;
import androidx.annotation.StringRes;
import androidx.annotation.WorkerThread;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

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
import java.security.UnrecoverableKeyException;
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
import javax.security.auth.Destroyable;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.SingleSubject;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;
import timber.log.Timber;

@Singleton
public class Crypto {
    public static final int SECURITY_BIO_VERSION = 1;

    public static final String PROTECTION_MODE_NONE = "none";
    public static final String PROTECTION_MODE_PIN = "pin";
    public static final String PROTECTION_MODE_PASSWORD = "password";
    public static final String PROTECTION_MODE_BIO_PIN = "bio_pin";
    public static final String PROTECTION_MODE_BIO_PASSWORD = "bio_password";

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS_PROTECTED = "NAProtected";
    private static final String KEY_ALIAS_BIOMETRIC = "NABiometric";
    private static final String KEY_MASTER_PASS = "key_pass";
    private static final String KEY_MASTER_BIO = "key_bio";
    private static final String SECURITY_VERSION_BIO_KEY = "sec_bio_ver";
    private static final String KEY_DB_PASS = "db_pass";
    private static final int SHARED_PREF_LOCK_TYPE_ID = R.string.settings_security_lock_key;
    private static final int SHARED_PREF_BIO_ENABLED_ID = R.string.settings_security_bio_key;

    private final String lockTypeKey, bioEnabledKey;

    @NonNull
    private final Context context;
    private final KeyguardManager keyguardManager;

    @NonNull
    private final CryptoPasswordHandler cryptoPasswordHandler;

    @NonNull
    private final DatabaseHandler dbHandler;

    @NonNull
    private final SharedPreferences sharedPrefs;

    private final Features features;
    private SecureRandom secureRandom = new SecureRandom();
    private KeyStore keyStore;

    @Inject
    Crypto(@NonNull Context context, @NonNull CryptoPasswordHandler cryptoPasswordHandler, @NonNull DatabaseHandler dbHandler, @NonNull SharedPreferences sharedPrefs) {
        this.context = context.getApplicationContext();
        this.cryptoPasswordHandler = cryptoPasswordHandler;
        this.dbHandler = dbHandler;
        this.sharedPrefs = sharedPrefs;

        lockTypeKey = context.getString(SHARED_PREF_LOCK_TYPE_ID);
        bioEnabledKey = context.getString(SHARED_PREF_BIO_ENABLED_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager = context.getSystemService(KeyguardManager.class);
        } else {
            keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }

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

    public boolean hasLock() {
        return !context.getString(R.string.settings_prot_none_value).equals(getLockType());
    }

    @ProtectionMode
    public String getLockType() {
        return sharedPrefs.getString(lockTypeKey, context.getString(R.string.settings_prot_none_value));
    }

    public boolean isBioEnabled() {
        return sharedPrefs.getBoolean(bioEnabledKey, false);
    }

    @SuppressLint("ApplySharedPref")
    public Completable create(@NonNull char[] password, boolean usePin) {
        return Completable
                .fromCallable(() -> {
                    SecretKey master = null;
                    char[] dbPass = null;

                    try {
                        master = generateMasterKey();

                        Cipher masterCipher = getCipher(Cipher.ENCRYPT_MODE, master, false);

                        dbPass = generateDbPass();
                        String encryptedDbPass = encrypt(masterCipher, dbPass);

                        cryptoPasswordHandler.delete();
                        cryptoPasswordHandler.create(password, false);
                        cryptoPasswordHandler.getMasterKeyHandler().set(master.getEncoded());

                        boolean saved = sharedPrefs
                                .edit()
                                .putString(KEY_DB_PASS, encryptedDbPass)
                                //.putString(KEY_MASTER_PASS, passwordWrappedKey)
                                .putString(lockTypeKey, usePin ? PROTECTION_MODE_PIN : PROTECTION_MODE_PASSWORD)
                                .commit();
                        if (saved) {
                            if (dbHandler.isOpen()) {
                                dbHandler.changePassword(dbPass);
                            } else {
                                dbHandler.openDatabase(new String(dbPass)); // FIXME should use the char array
                            }
                        } else {
                            throw new CannotSaveKeyException();
                        }
                    } finally {
                        cryptoPasswordHandler.close();

                        if (dbPass != null) {
                            Arrays.fill(dbPass, (char) 0);
                        }

                        destroyKey(master);
                    }
                    return true;
                })
                .subscribeOn(Schedulers.computation());
    }

    @SuppressLint("ApplySharedPref")
    public Completable create_OLD(@NonNull char[] password, boolean usePin) {
        return Completable
                .fromCallable(() -> {
                            SecretKey master = null, passwordKey = null;
                            try {
                                master = generateMasterKey();
                                byte[] salt = new byte[16];
                                secureRandom.nextBytes(salt);

                                Cipher masterCipher = getCipher(Cipher.ENCRYPT_MODE, master, false);

                                passwordKey = generatePasswordKey(password, salt);

//                                try {
//                                    lastTime = SystemClock.elapsedRealtimeNanos();
//                                    byte[] argonKey = Argon2Factory.createAdvanced(16, 64).rawHash(10, 2048, 4, password, salt);
//                                    Timber.v("[TIMER] argonKey generated in %d ns, length: %d", SystemClock.elapsedRealtimeNanos() - lastTime, argonKey.length);
//                                } catch (Throwable t) {
//                                    Timber.e(t, "[TIMER] Argon2 error: %s", t.getMessage());
//                                }

                                Cipher passwordCipher = getCipher(Cipher.WRAP_MODE, passwordKey, false);
                                String passwordWrappedKey = Base64.encodeToString(salt, Base64.NO_PADDING | Base64.NO_WRAP) + '.' + wrapKeyAndEncode(master, passwordCipher);

                                char[] dbPass = generateDbPass();
                                String encryptedDbPass = encrypt(masterCipher, dbPass);

                                try {
                                    Timber.v("Deleting (PRE) CryptoDatabase: %s", cryptoPasswordHandler.delete());
                                    Timber.v("Creating CryptoDatabase");
                                    cryptoPasswordHandler.create("fakepass".toCharArray(), true);
                                    cryptoPasswordHandler.close();
                                    Timber.v("Created CryptoDatabase");

                                    Timber.v("Trying auth with good pass");
                                    cryptoPasswordHandler.authenticate("fakepass".toCharArray());
                                    //cryptoPasswordHandler.close();
                                    Timber.v("Good pass SUCCESS");
                                    cryptoPasswordHandler.getMasterKeyHandler().set(master.getEncoded());
                                    cryptoPasswordHandler.close();

                                    SecretKey masterUnwrapped = new SecretKeySpec(cryptoPasswordHandler.getMasterKey("fakepass".toCharArray()), "AES");
                                    Cipher masterDecryptCipher = getCipher(Cipher.DECRYPT_MODE, masterUnwrapped, encryptedDbPass);
                                    char[] decrypted = decryptToChars(masterDecryptCipher, encryptedDbPass);

                                    Timber.v("#1 pass: %s", Arrays.toString(dbPass));
                                    Timber.v("#2 encrypted pass: %s", encryptedDbPass);
                                    Timber.v("#3 pass: %s", Arrays.toString(decrypted));

                                    /*Timber.v("Trying auth with bad pass");
                                    cryptoPasswordHandler.authenticate("wrongpass".toCharArray());
                                    cryptoPasswordHandler.close();
                                    Timber.v("Bad pass SUCCESS");*/
                                } catch (Throwable t) {
                                    Timber.e(t, "CryptoDatabase test error: %s", t.getMessage());
                                } finally {
                                    Timber.v("Deleting (POST) CryptoDatabase: %s", cryptoPasswordHandler.delete());
                                }

                                // --- TESTING ---
                                /*String part = passwordWrappedKey.split("\\.")[1];
                                Cipher passwordUnwrapCipher = getCipher(Cipher.UNWRAP_MODE, passwordKey, part);
                                SecretKey masterUnwrapped = unwrapKey(part, passwordUnwrapCipher);
                                Cipher masterDecryptCipher = getCipher(Cipher.DECRYPT_MODE, masterUnwrapped, encryptedDbPass);
                                char[] decrypted = decryptToChars(masterDecryptCipher, encryptedDbPass);

                                Timber.v("#1 pass: %s", Arrays.toString(dbPass));
                                Timber.v("#2 encrypted pass: %s", encryptedDbPass);
                                Timber.v("#3 pass: %s", Arrays.toString(decrypted));*/

                                /*boolean saved = sharedPrefs
                                        .edit()
                                        .putString(KEY_DB_PASS, encryptedDbPass)
                                        .putString(KEY_MASTER_PASS, passwordWrappedKey)
                                        .putString(lockTypeKey, usePin ? PROTECTION_MODE_PIN : PROTECTION_MODE_PASSWORD)
                                        .commit();
                                if (saved) {
                                    if (dbHandler.isOpen()) {
                                        dbHandler.changePassword(dbPass);
                                    } else {
                                        dbHandler.openDatabase(new String(dbPass)); // FIXME should use the char array
                                    }
                                } else {
                                    throw new CannotSaveKeyException();
                                }*/

                                Arrays.fill(dbPass, (char) 0);
                            } finally {
                                try {
                                    if (master != null && !master.isDestroyed()) {
                                        master.destroy();
                                    }
                                } catch (DestroyFailedException e) {
                                    Timber.e(e, "Cannot destroy master key: %s", e.getMessage());
                                }

                                try {
                                    if (passwordKey != null && !passwordKey.isDestroyed()) {
                                        passwordKey.destroy();
                                    }
                                } catch (DestroyFailedException e) {
                                    Timber.e(e, "Cannot destroy password key: %s", e.getMessage());
                                }
                            }

                            return true;
                        }
                )
                .subscribeOn(Schedulers.computation());
    }

    public Completable create(@NonNull Fragment fragment, @NonNull char[] password) {
        return create(fragment.requireActivity(), password);
    }

    public Completable create(@NonNull FragmentActivity activity, @NonNull char[] password) {
        return Completable
                .fromCallable(() -> {
                    createBio(activity, password).blockingAwait();
                    return true;
                })
                .subscribeOn(Schedulers.computation());
    }

    @SuppressLint({"CheckResult", "ApplySharedPref"})
    @TargetApi(Build.VERSION_CODES.M)
    @WorkerThread
    private Completable createBio(@NonNull FragmentActivity activity, @NonNull char[] password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, NoSuchPaddingException {
        CompletableSubject subject = CompletableSubject.create();

        SecretKey biometricKey;
        Cipher biometricCipher;

        try {
            sharedPrefs.edit().putInt(SECURITY_VERSION_BIO_KEY, SECURITY_BIO_VERSION).commit();

            try {
                deleteBiometricKey();
            } catch (Throwable t) {
                Timber.w(t, "Error");
            }
            biometricKey = generateBiometricKey();
            biometricCipher = getCipher(Cipher.WRAP_MODE, biometricKey, true);
        } catch (InvalidAlgorithmParameterException e2) {
            int bioResults = getFeatures().getBiometricsResults();
            if (bioResults == Features.BIO_NO_FINGERPRINTS || bioResults == Features.BIO_DEVICE_NOT_SECURE) {
                throw new BiometricException(BiometricConstants.ERROR_NO_BIOMETRICS, e2.getMessage());
            } else {
                throw new BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e2.getMessage());
            }
        } catch (KeyPermanentlyInvalidatedException e) {
            Timber.e(e, "Cannot use bio key");
            removeBio().blockingAwait();
            throw new BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e.getMessage());
            /*// retrying
            try {
                biometricKey = generateBiometricKey();
                biometricCipher = getCipher(Cipher.WRAP_MODE, biometricKey, true);
            } catch (InvalidAlgorithmParameterException e2) {
                int bioResults = getFeatures().getBiometricsResults();
                if (bioResults == Features.BIO_NO_FINGERPRINTS || bioResults == Features.BIO_DEVICE_NOT_SECURE) {
                    throw new BiometricException(BiometricConstants.ERROR_NO_BIOMETRICS, e2.getMessage());
                } else {
                    throw new BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e2.getMessage());
                }
            } catch (KeyPermanentlyInvalidatedException e2) {
                Timber.e(e2, "Cannot use bio key 2");
                removeBio().blockingAwait();
                throw new BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e2.getMessage());
            }*/
        }

        final SecretKey biometricKeyFin = biometricKey;

        authenticateBiometric(activity, biometricCipher, R.string.security_biometrics_prompt_title, android.R.string.cancel, false)
                .doAfterTerminate(() -> destroyKey(biometricKeyFin))
                .flatMapCompletable(cryptoObject -> {
                    SecretKey master = null;
                    byte[] key = null;
                    char[] dbPass = null;

                    try {
                        cryptoPasswordHandler.authenticate(password);
                        String encryptedDbPass = sharedPrefs.getString(KEY_DB_PASS, null);
                        key = cryptoPasswordHandler.getMasterKeyHandler().get();
                        master = new SecretKeySpec(key, "AES");
                        Cipher masterDecryptCipher = getCipher(Cipher.DECRYPT_MODE, master, encryptedDbPass);
                        dbPass = decryptToChars(masterDecryptCipher, encryptedDbPass);
                        dbHandler.openDatabase(new String(dbPass));

                        String biometricWrappedKey;
                        try {
                            biometricWrappedKey = wrapKeyAndEncode(master, cryptoObject.getCipher());
                        } catch (IllegalBlockSizeException e) {
                            Timber.e(e, "Biometric key error");
                            removeBio().blockingAwait();
                            throw new BiometricException(BiometricException.ERROR_KEY_INVALIDATED, "");
                        }

                        if (sharedPrefs.edit().putString(KEY_MASTER_BIO, biometricWrappedKey).putBoolean(bioEnabledKey, true).commit()) {
                            return Completable.complete();
                        } else {
                            return Completable.error(new IllegalStateException("Could not save bio master key"));
                        }
                    } finally {
                        cryptoPasswordHandler.close();

                        if (key != null) {
                            Arrays.fill(key, (byte) 0);
                        }

                        if (dbPass != null) {
                            Arrays.fill(dbPass, (char) 0);
                        }

                        destroyKey(master);
                    }
                })
                .subscribe(subject);

        return subject.hide();
    }

    @SuppressLint("ApplySharedPref")
    public Completable remove(@NonNull char[] password) {
        return Completable
                .fromCallable(() -> {
                    SecretKey master = null;
                    byte[] key = null;
                    char[] dbPass = null;

                    try {
                        String encryptedDbPass = sharedPrefs.getString(KEY_DB_PASS, null);
                        key = cryptoPasswordHandler.getMasterKey(password);
                        master = new SecretKeySpec(key, "AES");
                        Cipher masterDecryptCipher = getCipher(Cipher.DECRYPT_MODE, master, encryptedDbPass);
                        dbPass = decryptToChars(masterDecryptCipher, encryptedDbPass);

                        dbHandler.openDatabase(new String(dbPass)); // FIXME needs char array
                        dbHandler.changePassword("fakepass".toCharArray());

                        sharedPrefs
                                .edit()
                                .remove(KEY_DB_PASS)
                                //.remove(KEY_MASTER_PASS)
                                .remove(KEY_MASTER_BIO)
                                .putString(lockTypeKey, PROTECTION_MODE_NONE)
                                .putBoolean(bioEnabledKey, false)
                                .commit();

                        removeBio().blockingAwait();

                        return true;
                    } finally {
                        cryptoPasswordHandler.close();

                        if (key != null) {
                            Arrays.fill(key, (byte) 0);
                        }

                        if (dbPass != null) {
                            Arrays.fill(dbPass, (char) 0);
                        }

                        destroyKey(master);
                    }
                })
                .subscribeOn(Schedulers.computation());
    }

    public Completable removeBio() {
        return Completable
                .fromCallable(() -> {
                    boolean saved = sharedPrefs
                            .edit()
                            .remove(KEY_MASTER_BIO)
                            .putBoolean(bioEnabledKey, false)
                            .commit();

                    if (saved) {
                        Timber.v("Removing biometrics key");
                        deleteBiometricKey();

                        return true;
                    } else {
                        throw new CannotSaveKeyException();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @SuppressLint("ApplySharedPref")
    public Completable remove_OLD(@NonNull char[] password) {
        return Completable
                .fromCallable(() -> {
                    SecretKey passwordKey = null, masterUnwrapped = null;
                    try {
                        byte[] salt, key;

                        byte[][] parts = getMasterParts();
                        salt = parts[0];
                        key = parts[1];


                        passwordKey = generatePasswordKey(password, salt);
                        Cipher passwordCipher = getCipher(Cipher.UNWRAP_MODE, passwordKey, key);
                        String encryptedDbPass = sharedPrefs.getString(KEY_DB_PASS, null);
                        masterUnwrapped = unwrapKey(key, passwordCipher);
                        Cipher masterDecryptCipher = getCipher(Cipher.DECRYPT_MODE, masterUnwrapped, encryptedDbPass);
                        char[] dbPass = decryptToChars(masterDecryptCipher, encryptedDbPass);

                        dbHandler.openDatabase(new String(dbPass)); // FIXME needs char array
                        dbHandler.changePassword("fakepass".toCharArray());

                        // TODO remove bio key from AndroidKeyStore

                        sharedPrefs
                                .edit()
                                .remove(KEY_DB_PASS)
                                .remove(KEY_MASTER_PASS)
                                .remove(KEY_MASTER_BIO)
                                .putString(lockTypeKey, PROTECTION_MODE_NONE)
                                .putBoolean(bioEnabledKey, false)
                                .commit();

                        Arrays.fill(dbPass, (char) 0);

                        return true;
                    } finally {
                        if (passwordKey != null && !passwordKey.isDestroyed()) {
                            try {
                                passwordKey.destroy();
                            } catch (DestroyFailedException e) {
                                Timber.e(e, "Cannot destroy password key: %s", e.getMessage());
                            }
                        }

                        if (masterUnwrapped != null && !masterUnwrapped.isDestroyed()) {
                            try {
                                masterUnwrapped.destroy();
                            } catch (DestroyFailedException e) {
                                Timber.e(e, "Cannot destroy master key: %s", e.getMessage());
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.computation());
    }

    public Completable authenticate(@NonNull char[] password, boolean openDatabase) {
        return Completable
                .fromCallable(() -> {
                    SecretKey master = null;
                    byte[] key = null;
                    char[] dbPass = null;

                    try {
                        cryptoPasswordHandler.authenticate(password);
                        if (openDatabase) {
                            String encryptedDbPass = sharedPrefs.getString(KEY_DB_PASS, null);
                            key = cryptoPasswordHandler.getMasterKeyHandler().get();
                            master = new SecretKeySpec(key, "AES");
                            Cipher masterDecryptCipher = getCipher(Cipher.DECRYPT_MODE, master, encryptedDbPass);
                            dbPass = decryptToChars(masterDecryptCipher, encryptedDbPass);
                            dbHandler.openDatabase(new String(dbPass));
                        }
                    } finally {
                        cryptoPasswordHandler.close();

                        if (key != null) {
                            Arrays.fill(key, (byte) 0);
                        }

                        if (dbPass != null) {
                            Arrays.fill(dbPass, (char) 0);
                        }

                        destroyKey(master);
                    }

                    return true;
                })
                .subscribeOn(Schedulers.computation());
    }

    public Completable authenticate_OLD(@NonNull char[] password, boolean openDatabase) {
        byte[] salt, key;
        try {
            byte[][] parts = getMasterParts();
            salt = parts[0];
            key = parts[1];
        } catch (Throwable t) {
            return Completable.error(t);
        }

        /*String wrapped = sharedPrefs.getString(KEY_MASTER_PASS, null);
        if (wrapped == null || wrapped.indexOf('.') < 0) {
            return Completable.error(new IllegalStateException("No password key found"));
        }

        String[] parts = wrapped.split("\\.");

        if (parts.length != 2) {
            return Completable.error(new IllegalStateException("Invalid password key found"));
        }

        byte[] salt = Base64.decode(parts[0], Base64.NO_PADDING | Base64.NO_WRAP);*/

        return Completable
                .fromCallable(() -> {
                    SecretKey passwordKey = null, masterUnwrapped = null;
                    try {
                        passwordKey = generatePasswordKey(password, salt);
                        Cipher passwordCipher = getCipher(Cipher.UNWRAP_MODE, passwordKey, key);

                        if (openDatabase) {
                            String encryptedDbPass = sharedPrefs.getString(KEY_DB_PASS, null);
                            masterUnwrapped = unwrapKey(key, passwordCipher);
                            Cipher masterDecryptCipher = getCipher(Cipher.DECRYPT_MODE, masterUnwrapped, encryptedDbPass);
                            char[] dbPass = decryptToChars(masterDecryptCipher, encryptedDbPass);

                            dbHandler.openDatabase(new String(dbPass));

                            Arrays.fill(dbPass, '\0');
                        }
                    } finally {
                        if (passwordKey != null && !passwordKey.isDestroyed()) {
                            try {
                                passwordKey.destroy();
                            } catch (DestroyFailedException e) {
                                Timber.e(e, "Cannot destroy password key: %s", e.getMessage());
                            }
                        }
                        if (masterUnwrapped != null && !masterUnwrapped.isDestroyed()) {
                            try {
                                masterUnwrapped.destroy();
                            } catch (DestroyFailedException e) {
                                Timber.e(e, "Cannot destroy master key: %s", e.getMessage());
                            }
                        }
                    }

                    return true;
                })
                .subscribeOn(Schedulers.computation());
    }

    @TargetApi(Build.VERSION_CODES.M)
    public Completable authenticate(@NonNull Fragment fragment, boolean isIntermediate) {
        return authenticate(fragment.requireActivity(), isIntermediate);
    }

    @SuppressLint("ApplySharedPref")
    @TargetApi(Build.VERSION_CODES.M)
    public Completable authenticate(@NonNull FragmentActivity activity, boolean isIntermediate) {
        return Completable
                .fromCallable(() -> {
                    if (sharedPrefs.getInt(SECURITY_VERSION_BIO_KEY, 0) != SECURITY_BIO_VERSION) {
                        removeBio().blockingAwait();
                        sharedPrefs.edit().putInt(SECURITY_VERSION_BIO_KEY, SECURITY_BIO_VERSION).commit();
                        throw new BiometricException(BiometricException.ERROR_KEY_SECURITY_UPDATE, "New security version");
                    }

                    String masterWrapped = sharedPrefs.getString(KEY_MASTER_BIO, null);

                    SecretKey biometricKey;

                    Cipher rawBioCipher;
                    try {
                        biometricKey = getBiometricKey();
                        rawBioCipher = getCipher(Cipher.UNWRAP_MODE, biometricKey, masterWrapped);
                    } catch (InvalidKeyException | UnrecoverableKeyException e) {
                        removeBio().blockingAwait();
                        throw new BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e.getMessage());
                    }

                    SecretKey master = null;
                    byte[] key = null;
                    char[] dbPass = null;

                    try {
                        String encryptedDbPass = sharedPrefs.getString(KEY_DB_PASS, null);

                        BiometricPrompt.CryptoObject cryptoObject = authenticateBiometric(activity, rawBioCipher, R.string.security_biometrics_prompt_title, R.string.security_biometrics_prompt_negative_btn, isIntermediate).blockingGet();
                        Cipher biometricCipher = cryptoObject.getCipher();
                        try {
                            master = unwrapKey(masterWrapped, biometricCipher);
                        } catch (InvalidKeyException e) {
                            Timber.e(e, "Biometric key error");
                            removeBio().blockingAwait();
                            throw new BiometricException(BiometricException.ERROR_KEY_INVALIDATED, "");
                        }

                        Cipher masterDecryptCipher = getCipher(Cipher.DECRYPT_MODE, master, encryptedDbPass);
                        dbPass = decryptToChars(masterDecryptCipher, encryptedDbPass);
                        dbHandler.openDatabase(new String(dbPass));
                    } finally {
                        cryptoPasswordHandler.close();

                        if (key != null) {
                            Arrays.fill(key, (byte) 0);
                        }

                        if (dbPass != null) {
                            Arrays.fill(dbPass, (char) 0);
                        }

                        destroyKey(biometricKey);
                        destroyKey(master);
                    }
                    return true;
                })
                .subscribeOn(Schedulers.computation());
    }

    private Single<BiometricPrompt.CryptoObject> authenticateBiometric(@NonNull FragmentActivity activity, @NonNull Cipher cipher, @StringRes int title, @StringRes int negativeBtn, boolean isIntermediate) {
        SingleSubject<BiometricPrompt.CryptoObject> subject = SingleSubject.create();

        BiometricPrompt.PromptInfo.Builder promptInfoBuilder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(title))
                .setNegativeButtonText(context.getString(negativeBtn));

        if (isIntermediate) {
            promptInfoBuilder.setDescription(context.getString(R.string.security_intermediate_message));
        }

        BiometricPrompt.PromptInfo promptInfo = promptInfoBuilder.build();

        BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);

        BiometricPrompt prompt = Single
                .defer(() -> {
                    BiometricPrompt deferredPrompt = new BiometricPrompt(activity, Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            Timber.d("[AUTH] error: %d, str: %s", errorCode, errString);
                            if (!subject.hasThrowable()) {
                                subject.onError(new BiometricException(errorCode, errString));
                            }
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            Timber.d("[AUTH] success: %s", result);

                            //noinspection ConstantConditions
                            subject.onSuccess(result.getCryptoObject());
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            Timber.d("[AUTH] failed");
                            //subject.onError(new AuthenticationFailedException());
                        }
                    });
                    return Single.just(deferredPrompt);
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .blockingGet();

        /*BiometricPrompt prompt = new BiometricPrompt(activity, Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Timber.d("[AUTH] error: %d, str: %s", errorCode, errString);
                if (!subject.hasThrowable()) {
                    subject.onError(new BiometricException(errorCode, errString));
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                Timber.d("[AUTH] success: %s", result);

                //noinspection ConstantConditions
                subject.onSuccess(result.getCryptoObject());
            }

            @Override
            public void onAuthenticationFailed() {
                Timber.d("[AUTH] failed");
                //subject.onError(new AuthenticationFailedException());
            }
        });*/

        final DefaultLifecycleObserver retryObserver = new DefaultLifecycleObserver() {
            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                owner.getLifecycle().removeObserver(this);

                if (!subject.hasValue() && !subject.hasThrowable()) {
                    prompt.cancelAuthentication();
                    subject.onError(new BiometricException(BiometricException.ERROR_SHOULD_RETRY, ""));
                }
            }
        };

        Disposable disposable = Single.just(prompt)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(biometricPrompt -> {
                    Lifecycle.State state = activity.getLifecycle().getCurrentState();
                    if (state.isAtLeast(Lifecycle.State.STARTED)) {
                        biometricPrompt.authenticate(promptInfo, cryptoObject);
                        activity.getLifecycle().addObserver(retryObserver);
                    } else {
                        subject.onError(new BiometricException(BiometricException.ERROR_SHOULD_RETRY, ""));
                    }
                });


        return subject
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> activity.getLifecycle().removeObserver(retryObserver))
                .doOnDispose(() -> {
                    prompt.cancelAuthentication();

                    if (!disposable.isDisposed()) {
                        disposable.dispose();
                    }
                })
                .hide();
    }

    @SuppressWarnings("ConstantConditions")
    private void destroyKey(@Nullable SecretKey key) {
        if (key != null && Destroyable.class.isAssignableFrom(key.getClass()) && !key.isDestroyed()) {
            try {
                key.destroy();
            } catch (DestroyFailedException ignored) {
            }
        }
    }

    private char[] generateDbPass() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);

        byte[] encoded = Base64.encode(bytes, Base64.NO_PADDING | Base64.NO_WRAP);

        Arrays.fill(bytes, (byte) 0);

        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded));
        char[] data = Arrays.copyOf(charBuffer.array(), charBuffer.limit());

        Arrays.fill(encoded, (byte) 0);

        return data;
    }

    @Size(2)
    private byte[][] getMasterParts() {
        String wrapped = sharedPrefs.getString(KEY_MASTER_PASS, null);
        if (wrapped == null || wrapped.indexOf('.') < 0) {
            throw new IllegalStateException("No password key found");
        }

        String[] parts = wrapped.split("\\.");

        if (parts.length != 2) {
            throw new IllegalStateException("Invalid password key found");
        }

        byte[] salt = Base64.decode(parts[0], Base64.NO_PADDING | Base64.NO_WRAP);
        byte[] key = Base64.decode(parts[1], Base64.NO_PADDING | Base64.NO_WRAP);

        return new byte[][]{salt, key};
    }

    // --------------------------------------
    // --------------------------------------
    // --------------------------------------
    // --------------------------------------
    // --------------------------------------

    public Features getFeatures() {
        return features;
    }

    private boolean isDeviceSecure() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return keyguardManager.isDeviceSecure();
        } else {
            return keyguardManager.isKeyguardSecure();
        }
    }

    /*public void authenticate(@NonNull FragmentActivity activity) {
        authenticate(activity, AUTH_MODE_BIOMETRIC);
    }*/

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
        System.arraycopy(data, 1, iv, 0, ivLength);

        //byte[] encrypted = new byte[data.length - iv.length - 1];
        //System.arraycopy(data, 1 + ivLength, encrypted, 0, encrypted.length);

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
        cipher.init(purpose, secretKey, parameterSpec);

        return cipher;
    }

    private SecretKey generateMasterKey() {
        // creating AES
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return new SecretKeySpec(key, "AES");
    }

    private SecretKey generatePasswordKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(password, salt, 65536, 256); //iterationCount: 65536
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        spec.clearPassword();

        return secret;
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey getOrCreateBiometricKey() throws NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException {
        SecretKey key = null;
        try {
            key = getBiometricKey();
        } catch (UnrecoverableKeyException e) {
            removeBio().blockingAwait();
        }

        if (key == null) {
            key = generateBiometricKey();
        }

        return key;
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey getBiometricKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return (SecretKey) keyStore.getKey(KEY_ALIAS_BIOMETRIC, null);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void deleteBiometricKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        keyStore.deleteEntry(KEY_ALIAS_BIOMETRIC);
        keyStore.load(null);
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey generateBiometricKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenParameterSpec.Builder specBuilder = new KeyGenParameterSpec.Builder(KEY_ALIAS_BIOMETRIC, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setUserAuthenticationRequired(true)
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            specBuilder.setInvalidatedByBiometricEnrollment(true);
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

    @SuppressWarnings("WeakerAccess")
    public class Features {
        public static final int BIO_OK = 0;
        public static final int BIO_DEVICE_NOT_SECURE = 1;
        public static final int BIO_NO_FINGERPRINTS = 2;
        public static final int BIO_NO_HARDWARE = 3;
        public static final int BIO_UNAVAILABLE_NOW = 4;

        public boolean isBiometricsSupported() {
            return getBiometricsResults() != BIO_NO_HARDWARE;
        }

        @SuppressWarnings("deprecation")
        public int getBiometricsResults() {
            FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(context);
            if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                return BIO_NO_HARDWARE;
            } else if (!isDeviceSecure()) {
                return BIO_DEVICE_NOT_SECURE;
            } else if (!fingerprintManager.isHardwareDetected()) {
                return BIO_UNAVAILABLE_NOW;
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                return BIO_NO_FINGERPRINTS;
            }

            return BIO_OK;
        }
    }
}
