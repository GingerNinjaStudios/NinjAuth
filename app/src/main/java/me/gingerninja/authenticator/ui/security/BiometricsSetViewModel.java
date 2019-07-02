package me.gingerninja.authenticator.ui.security;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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

    @NonNull
    private final Crypto crypto;

    private CompositeDisposable disposable = new CompositeDisposable();

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    public boolean isBioEnabled;
    public ObservableBoolean inputEnabled = new ObservableBoolean(true);

    @Inject
    BiometricsSetViewModel(@NonNull Crypto crypto) {
        this.crypto = crypto;
        isBioEnabled = crypto.isBioEnabled();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
    }

    public void onAuthClick(View v) {
        inputEnabled.set(false);

        if (isBioEnabled) {
            events.setValue(new SingleEvent(EVENT_DISABLE));
        } else {
            events.setValue(new SingleEvent(EVENT_ENABLE));
        }
    }

    public void onSkipClick(View v) {
        events.setValue(new SingleEvent(EVENT_SKIP));
    }

    public LiveData<SingleEvent> getEvents() {
        return events;
    }

    void createBiometrics(Fragment fragment, char[] password) {
        disposable.clear();

        disposable.add(
                crypto.create(fragment, password)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                                    events.setValue(new SingleEvent(EVENT_ENABLE_SUCCESS));
                                },
                                throwable -> {
                                    inputEnabled.set(true);
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
