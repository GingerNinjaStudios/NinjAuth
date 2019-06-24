package me.gingerninja.authenticator.ui.backup;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.SingleEvent;
import me.gingerninja.authenticator.util.backup.Backup;
import me.gingerninja.authenticator.util.backup.BackupUtils;

public class BackupViewModel extends ViewModel {
    static final String EVENT_CREATE_BACKUP = "event.createBackup";
    //public static final String EVENT_NAV_ACCOUNTS = "event.nav.accounts";
    //public static final String EVENT_NAV_LABELS = "event.nav.labels";

    public Data data = new Data();
    public Error error = new Error();
    public ObservableBoolean inputsEnabled = new ObservableBoolean(true);

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    private CompositeDisposable disposable = new CompositeDisposable();

    @NonNull
    private AccountRepository repository;

    private BackupUtils backupUtils;

    private BehaviorSubject<Backup.Progress> backupProgress;

    @Inject
    BackupViewModel(@NonNull AccountRepository repository, @NonNull BackupUtils backupUtils) {
        this.repository = repository;
        this.backupUtils = backupUtils;

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

    LiveData<SingleEvent> getEvents() {
        return events;
    }

    public void onBackupClick(View v) {
        events.setValue(new SingleEvent(EVENT_CREATE_BACKUP));
    }

    public void onAccountImagesClick(View v) {
        data.accountImages.set(!data.accountImages.get());
    }

    public Observable<Backup.Progress> observeProgress() {
        return backupProgress.hide().observeOn(AndroidSchedulers.mainThread());
    }

    public void handleBackupPickerResults(Intent intent) {
        if (backupProgress != null) {
            backupProgress.onComplete();
        }

        backupProgress = BehaviorSubject.createDefault(new Backup.Progress(Backup.Progress.PHASE_DATA_FILE, 0, 0));

        Uri uri = backupUtils.getUriFromIntent(intent);

        String rawPass = data.pass.get();
        char[] pass = TextUtils.isEmpty(rawPass) ? null : rawPass.toCharArray();

        Backup.Options options = new Backup.Options.Builder()
                .password(pass)
                .withAccountImages(data.accountImages.get())
                .setComment(data.comment.get())
                .setAutoBackup(false)
                .build();

        inputsEnabled.set(false);

        backupUtils.backup(uri)
                .export(options)
                .doOnError(throwable -> inputsEnabled.set(true))
                .subscribe(backupProgress);
    }

    /*public void onAccountsSelectorClick(View v) {
        events.setValue(new SingleEvent(EVENT_NAV_ACCOUNTS));
    }

    public void onLabelsSelectorClick(View v) {
        events.setValue(new SingleEvent(EVENT_NAV_LABELS));
    }*/

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
