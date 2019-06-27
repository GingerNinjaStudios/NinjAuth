package me.gingerninja.authenticator.ui.security;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.util.SingleEvent;

public class PasswordCheckViewModel extends ViewModel {
    static final String EVENT_CONFIRM = "event.confirm";
    static final String EVENT_BIO_AUTH = "event.bio";

    public ObservableBoolean hasError = new ObservableBoolean(false);
    public ObservableInt errorText = new ObservableInt(0);

    public ObservableField<String> password = new ObservableField<>("");
    public ObservableBoolean inputEnabled = new ObservableBoolean(true);

    public boolean usePin = false;
    public boolean enableBioAuth = false;

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    @NonNull
    private final Crypto crypto;

    private CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    PasswordCheckViewModel(@NonNull Crypto crypto) {
        this.crypto = crypto;
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

    private void setUsePin(boolean usePin) {
        this.usePin = usePin;
        updateUi();
    }

    void setEnableBioAuth(boolean enableBioAuth) {
        this.enableBioAuth = enableBioAuth;
        updateUi();
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
