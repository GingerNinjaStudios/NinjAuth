package me.gingerninja.authenticator.ui.backup;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.SingleEvent;

public class BackupViewModel extends ViewModel {
    public static final String EVENT_CREATE_BACKUP = "event.createBackup";
    public static final String EVENT_NAV_ACCOUNTS = "event.nav.accounts";
    public static final String EVENT_NAV_LABELS = "event.nav.labels";

    public Data data = new Data();
    public Error error = new Error();

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    private CompositeDisposable disposable = new CompositeDisposable();

    @NonNull
    private AccountRepository repository;

    @Inject
    BackupViewModel(@NonNull AccountRepository repository) {
        this.repository = repository;

        disposable.addAll(
                this.repository
                        .getAccounts()
                        .count()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                value -> data.accountCount.set(value.intValue()),
                                throwable -> data.accountCount.set(Integer.MIN_VALUE)
                        ),
                this.repository
                        .getAllLabel()
                        .count()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                value -> data.labelCount.set(value.intValue()),
                                throwable -> data.labelCount.set(Integer.MIN_VALUE)
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

    public LiveData<SingleEvent> getEvents() {
        return events;
    }

    public void onBackupClick(View v) {
        events.setValue(new SingleEvent(EVENT_CREATE_BACKUP));
    }

    public void onAccountImagesClick(View v) {
        data.accountImages.set(!data.accountImages.get());
    }

    public void onAccountsSelectorClick(View v) {
        events.setValue(new SingleEvent(EVENT_NAV_ACCOUNTS));
    }

    public void onLabelsSelectorClick(View v) {
        events.setValue(new SingleEvent(EVENT_NAV_LABELS));
    }

    public class Data {
        public final ObservableInt accountCount = new ObservableInt(-1);
        public final ObservableInt labelCount = new ObservableInt(-1);

        public final ObservableField<String> pass = new ObservableField<>();
        public final ObservableField<String> comment = new ObservableField<>();
        public final ObservableBoolean accountImages = new ObservableBoolean(true);
    }

    public class Error {
        public final ObservableInt pass = new ObservableInt();
        public final ObservableInt comment = new ObservableInt();
    }
}
