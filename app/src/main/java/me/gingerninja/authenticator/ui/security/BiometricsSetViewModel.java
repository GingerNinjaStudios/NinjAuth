package me.gingerninja.authenticator.ui.security;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricConstants;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.crypto.BiometricException;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public class BiometricsSetViewModel extends ViewModel {
    static final String EVENT_ENABLE = "event.enable";
    static final String EVENT_ENABLE_SUCCESS = "event.enable.success";
    static final String EVENT_ENABLE_FAIL = "event.enable.fail";

    static final String EVENT_DISABLE = "event.disable";
    static final String EVENT_DISABLE_SUCCESS = "event.disable.success";
    static final String EVENT_DISABLE_FAIL = "event.disable.fail";

    static final String EVENT_SKIP = "event.skip";

    static final String EVENT_BIO_ERROR_ENROLL = "event.error.enroll";
    static final String EVENT_BIO_ERROR_UNLOCK = "event.error.unlock";

    @NonNull
    private final Crypto crypto;

    private CompositeDisposable disposable = new CompositeDisposable();

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    public ObservableBoolean isBioEnabled = new ObservableBoolean(false);
    public ObservableInt bioErrorText = new ObservableInt(0);
    public ObservableBoolean bioErrorBtnVisible = new ObservableBoolean(false);
    public ObservableInt bioErrorBtnText = new ObservableInt(0);
    public ObservableField<View.OnClickListener> bioErrorBtnClick = new ObservableField<>();

    public ObservableBoolean inputEnabled = new ObservableBoolean(true);

    @Inject
    BiometricsSetViewModel(@NonNull Crypto crypto) {
        this.crypto = crypto;
        isBioEnabled.set(crypto.isBioEnabled());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
    }

    public void onAuthClick(View v) {
        inputEnabled.set(false);

        if (isBioEnabled.get()) {
            events.setValue(new SingleEvent(EVENT_DISABLE));
        } else {
            events.setValue(new SingleEvent(EVENT_ENABLE));
        }
    }

    public void onSkipClick(View v) {
        events.setValue(new SingleEvent(EVENT_SKIP));
    }

    private void onBioErrorBtnClickForEnrollment(View v) {
        events.setValue(new SingleEvent(EVENT_BIO_ERROR_ENROLL));
    }

    private void onBioErrorBtnClickForUnlock(View v) {
        events.setValue(new SingleEvent(EVENT_BIO_ERROR_UNLOCK));
    }

    public LiveData<SingleEvent> getEvents() {
        return events;
    }

    void createBiometrics(Fragment fragment, char[] password) {
        disposable.clear();

        inputEnabled.set(false);
        bioErrorBtnVisible.set(false);
        bioErrorBtnText.set(0);
        bioErrorBtnClick.set(null);
        bioErrorText.set(0);

        disposable.add(
                crypto.create(fragment, password)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                                    events.setValue(new SingleEvent(EVENT_ENABLE_SUCCESS));
                                },
                                throwable -> {
                                    boolean enableInput = true;

                                    if (throwable instanceof BiometricException) {
                                        int errCode = ((BiometricException) throwable).getErrorCode();
                                        switch (errCode) {
                                            case BiometricException.ERROR_KEY_INVALIDATED:
                                                bioErrorText.set(R.string.biometric_error_key_invalidated);
                                                break;
                                            case BiometricException.ERROR_SHOULD_RETRY:
                                                enableInput = false;
                                                events.setValue(new SingleEvent(EVENT_ENABLE));
                                                break;
                                            case BiometricConstants.ERROR_LOCKOUT:
                                                bioErrorText.set(R.string.biometric_error_temporary_lock);
                                                break;
                                            case BiometricConstants.ERROR_LOCKOUT_PERMANENT:
                                                bioErrorText.set(R.string.biometric_error_permanent_lock);
                                                bioErrorBtnVisible.set(true);
                                                bioErrorBtnText.set(R.string.biometric_error_permanent_lock_btn);
                                                bioErrorBtnClick.set(this::onBioErrorBtnClickForEnrollment);
                                                break;
                                            case BiometricConstants.ERROR_NO_BIOMETRICS:
                                                bioErrorText.set(R.string.biometric_error_no_biometrics);
                                                bioErrorBtnVisible.set(true);
                                                bioErrorBtnText.set(R.string.biometric_error_no_biometrics_btn);
                                                bioErrorBtnClick.set(this::onBioErrorBtnClickForEnrollment);
                                                break;
                                            case BiometricConstants.ERROR_TIMEOUT:
                                            case BiometricConstants.ERROR_NEGATIVE_BUTTON:
                                            case BiometricConstants.ERROR_CANCELED:
                                            case BiometricConstants.ERROR_USER_CANCELED:
                                                bioErrorText.set(0);
                                                break;
                                            default:
                                                bioErrorText.set(R.string.biometric_error_unknown);
                                        }
                                    } else {
                                        bioErrorText.set(R.string.biometric_error_unknown);
                                    }

                                    inputEnabled.set(enableInput);
                                    Timber.v(throwable, "Bio error");
                                    events.setValue(new SingleEvent(EVENT_ENABLE_FAIL, throwable));
                                })
        );
    }

    void removeBiometrics() {
        disposable.clear();

        disposable.add(
                crypto.removeBio()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                                    events.setValue(new SingleEvent(EVENT_DISABLE_SUCCESS));
                                },
                                throwable -> {
                                    inputEnabled.set(true);
                                    Timber.v(throwable, "Bio error");
                                    events.setValue(new SingleEvent(EVENT_DISABLE_FAIL, throwable));
                                })
        );
    }
}
