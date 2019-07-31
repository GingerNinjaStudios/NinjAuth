package me.gingerninja.authenticator.ui.security;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricConstants;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.crypto.BiometricException;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public class StartupPasswordCheckViewModel extends ViewModel {
    static final String EVENT_CONFIRM = "event.confirm";
    static final String EVENT_BIO_AUTH = "event.bio";
    static final String EVENT_USE_PASSWORD = "event.use_pass";

    public ObservableBoolean hasError = new ObservableBoolean(false);
    public ObservableInt errorText = new ObservableInt(0);

    public ObservableField<String> password = new ObservableField<>("");
    public ObservableBoolean inputEnabled = new ObservableBoolean(true);

    public boolean usePin = false;
    public ObservableBoolean enableBioAuth = new ObservableBoolean(false);
    public ObservableInt bioErrorText = new ObservableInt(0);

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    public boolean isIntermediate = false;

    private DatabaseHandler dbHandler;

    @NonNull
    private final Crypto crypto;

    private CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    StartupPasswordCheckViewModel(@NonNull Crypto crypto, DatabaseHandler dbHandler) {
        this.crypto = crypto;
        this.dbHandler = dbHandler;
        enableBioAuth.set(crypto.isBioEnabled());
        String lockType = crypto.getLockType();
        setUsePin(Crypto.PROTECTION_MODE_PIN.equals(lockType) || Crypto.PROTECTION_MODE_BIO_PIN.equals(lockType));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
    }

    boolean hasLock() {
        return crypto.hasLock();
    }

    void openUnlockedDatabase() {
        dbHandler.openDatabaseDefaultPassword();
    }

    private void setUsePin(boolean usePin) {
        this.usePin = usePin;
        updateUi();
    }

    void setIntermediate(boolean intermediate) {
        isIntermediate = intermediate;
    }

    @NonNull
    LiveData<SingleEvent> getEvents() {
        return events;
    }

    public void onNextClick(View v) {
        inputEnabled.set(false);
        disposable.clear();

        disposable.add(
                validatePassword()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            events.setValue(new SingleEvent(EVENT_CONFIRM));
                        }, throwable -> {
                            // TODO show the error
                            inputEnabled.set(true);
                            hasError.set(true);
                            updateUi();
                        })
        );
    }

    public void onBioAuthClick(View v) {
        inputEnabled.set(false);
        events.setValue(new SingleEvent(EVENT_BIO_AUTH));
    }

    void bioAuthentication(Fragment fragment) {
        if (enableBioAuth.get()) {
            bioErrorText.set(0);
            inputEnabled.set(false);
            disposable.clear();
            disposable.add(
                    crypto.authenticate(fragment)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                        events.setValue(new SingleEvent(EVENT_CONFIRM));
                                    },
                                    throwable -> {
                                        boolean enableInput = true;

                                        if (throwable instanceof BiometricException) {
                                            int errCode = ((BiometricException) throwable).getErrorCode();
                                            switch (errCode) {
                                                case BiometricException.ERROR_KEY_INVALIDATED:
                                                    enableBioAuth.set(false);
                                                    bioErrorText.set(R.string.biometric_error_key_invalidated);
                                                    break;
                                                case BiometricException.ERROR_SHOULD_RETRY:
                                                    enableInput = false;
                                                    events.setValue(new SingleEvent(EVENT_BIO_AUTH));
                                                    break;
                                                case BiometricException.ERROR_KEY_SECURITY_UPDATE:
                                                    enableBioAuth.set(false);
                                                    bioErrorText.set(R.string.biometric_error_key_removed_security_update);
                                                    break;
                                                case BiometricConstants.ERROR_LOCKOUT:
                                                    enableBioAuth.set(true);
                                                    bioErrorText.set(R.string.biometric_error_temporary_lock);
                                                    break;
                                                case BiometricConstants.ERROR_LOCKOUT_PERMANENT:
                                                    enableBioAuth.set(true);
                                                    bioErrorText.set(R.string.biometric_error_permanent_lock);
                                                    break;
                                                case BiometricConstants.ERROR_NO_BIOMETRICS:
                                                    enableBioAuth.set(false);
                                                    bioErrorText.set(R.string.biometric_error_no_biometrics);
                                                    break;
                                                case BiometricConstants.ERROR_TIMEOUT:
                                                case BiometricConstants.ERROR_NEGATIVE_BUTTON:
                                                case BiometricConstants.ERROR_CANCELED:
                                                case BiometricConstants.ERROR_USER_CANCELED:
                                                    inputEnabled.set(true);
                                                    enableBioAuth.set(true);
                                                    bioErrorText.set(0);
                                                    events.postValue(new SingleEvent(EVENT_USE_PASSWORD));
                                                    break;
                                                default:
                                                    enableBioAuth.set(true);
                                                    bioErrorText.set(R.string.biometric_error_unknown);
                                            }
                                        } else {
                                            enableBioAuth.set(true);
                                            bioErrorText.set(R.string.biometric_error_unknown);
                                        }
                                        Timber.e(throwable, "Bio error");
                                        inputEnabled.set(enableInput);
                                    })
            );
        }
    }

    @SuppressWarnings("ConstantConditions")
    private Completable validatePassword() {
        return crypto.authenticate(password.get().toCharArray(), true);
    }

    public boolean onEditorDoneAction(TextView view, int actionId, KeyEvent event) {
        boolean enter = event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER);

        //noinspection ConstantConditions
        if ((actionId == EditorInfo.IME_ACTION_DONE || enter) &&
                password.get().length() >= 4) {
            onNextClick(view);
        }
        return true;
    }

    private void updateUi() {
        if (hasError.get()) {
            if (usePin) {
                errorText.set(R.string.auth_wrong_pin);
            } else {
                errorText.set(R.string.auth_wrong_password);
            }
            password.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    hasError.set(false);
                    errorText.set(0);
                    password.removeOnPropertyChangedCallback(this);
                    updateUi();
                }
            });
        }
    }
}
