package me.gingerninja.authenticator.ui.security;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.util.SingleEvent;

public class LockTypeSelectorViewModel extends ViewModel {
    public static final String EVENT_LOCK_REMOVED = "event.lockRemoved";
    public static final String EVENT_LOCK_REMOVE_FAILED = "event.lockRemoveFailed";

    @NonNull
    private final Crypto crypto;

    private final MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    private CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    public LockTypeSelectorViewModel(@NonNull Crypto crypto) {
        this.crypto = crypto;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
    }

    public LiveData<SingleEvent> getEvents() {
        return events;
    }

    @NonNull
    public String getLockType() {
        return crypto.getLockType();
    }

    public void removeLock(char[] password) {
        disposable.clear();

        disposable.add(
                crypto
                        .remove(password)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> events.setValue(new SingleEvent(EVENT_LOCK_REMOVED)),
                                throwable -> events.setValue(new SingleEvent(EVENT_LOCK_REMOVE_FAILED, throwable))
                        )
        );
    }
}
