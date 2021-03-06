package me.gingerninja.authenticator.ui.security;

import android.text.TextUtils;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public class PasswordSetViewModel extends ViewModel {
    static final String EVENT_NEXT = "event.next";

    public ObservableBoolean isConfirmStage = new ObservableBoolean(false);
    public ObservableBoolean hasError = new ObservableBoolean(false);

    public ObservableField<String> password = new ObservableField<>("");
    public ObservableBoolean inputEnabled = new ObservableBoolean(true);

    public ObservableInt tvTitle = new ObservableInt();
    public ObservableInt tvMessage = new ObservableInt();
    public ObservableInt tvHelper = new ObservableInt();

    public boolean usePin = false;

    private String setPassword;

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    private CompositeDisposable disposable = new CompositeDisposable();

    @NonNull
    private final Crypto crypto;

    @Inject
    PasswordSetViewModel(@NonNull Crypto crypto) {
        this.crypto = crypto;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
    }

    void setUsePin(boolean usePin) {
        this.usePin = usePin;
        updateUi();
    }

    @NonNull
    LiveData<SingleEvent> getEvents() {
        return events;
    }

    public void onNextClick(View v) {
        if (isConfirmStage.get()) {
            if (TextUtils.equals(setPassword, password.get())) {
                inputEnabled.set(false);
                doCrypto();
            } else {
                hasError.set(true);
                updateUi();
            }
        } else {
            setPassword = password.get();
            password.set("");
            isConfirmStage.set(true);
            updateUi();
        }
    }

    private void doCrypto() {
        disposable.clear();
        disposable.add(
                crypto
                        .create(setPassword.toCharArray(), usePin)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> events.setValue(new SingleEvent(EVENT_NEXT)),
                                throwable -> {
                                    Timber.e(throwable, "Could not create password crypto: %s", throwable.getMessage());
                                    inputEnabled.set(true);
                                    // TODO error handling
                                })
        );
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
        if (!isConfirmStage.get()) {
            tvTitle.set(R.string.security_first_password_title);
            if (usePin) {
                tvMessage.set(R.string.security_first_password_message_pin);
                tvHelper.set(R.string.security_pin_helper);
            } else {
                tvMessage.set(R.string.security_first_password_message_password);
                tvHelper.set(R.string.security_password_helper);
            }
        } else {
            if (hasError.get()) {
                if (usePin) {
                    tvTitle.set(R.string.security_first_error_pin_match);
                } else {
                    tvTitle.set(R.string.security_first_error_password_match);
                }
                password.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(Observable sender, int propertyId) {
                        hasError.set(false);
                        password.removeOnPropertyChangedCallback(this);
                        updateUi();
                    }
                });
            } else {
                if (usePin) {
                    tvTitle.set(R.string.security_first_password_message_confirm_pin);
                    tvMessage.set(R.string.misc_empty);
                    tvHelper.set(0);
                } else {
                    tvTitle.set(R.string.security_first_password_message_confirm_password);
                    tvMessage.set(R.string.misc_empty);
                    tvHelper.set(0);
                }
            }
        }
    }
}
