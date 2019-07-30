package me.gingerninja.authenticator.ui.backup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.exception.ZipExceptionConstants;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.requery.query.Tuple;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.TempAccount;
import me.gingerninja.authenticator.data.db.entity.TempLabel;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.util.SingleEvent;
import me.gingerninja.authenticator.util.backup.BackupMeta;
import me.gingerninja.authenticator.util.backup.BackupUtils;
import me.gingerninja.authenticator.util.backup.NotNinjAuthZipFile;
import me.gingerninja.authenticator.util.backup.Restore;
import timber.log.Timber;

public class RestoreViewModel extends ViewModel {
    static final String ACTION_RESTORE_PASSWORD_NEEDED = "restore-password-needed";
    static final String ACTION_RESTORE_WRONG_PASSWORD = "restore-wrong-password";

    static final String ACTION_DATA_LOADED = "restore-data-loaded";
    //static final String ACTION_RESTORE_COMPLETE = "restore-complete";
    @NonNull
    private final BackupUtils backupUtils;

    @NonNull
    private final TemporaryRepository tempRepo;

    private AtomicInteger requestCounter = new AtomicInteger(0);

    @NonNull
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @NonNull
    private CompositeDisposable counterDisposable = new CompositeDisposable();

    @Nullable
    private Restore restoreInProgress;

    @Nullable
    private Uri uri;

    @NonNull
    private BehaviorSubject<SingleEvent> restoreSubject = BehaviorSubject.create();

    public ObservableBoolean inputEnabled = new ObservableBoolean(true);

    public ObservableBoolean processingFile = new ObservableBoolean(true);
    public ObservableInt errorMsg = new ObservableInt();

    public ObservableField<BackupMeta> meta = new ObservableField<>();

    public ObservableInt accountTotal = new ObservableInt();
    public ObservableInt accountSelected = new ObservableInt();

    public ObservableInt labelTotal = new ObservableInt();
    public ObservableInt labelSelected = new ObservableInt();

    @SuppressLint("CheckResult")
    @Inject
    RestoreViewModel(@NonNull BackupUtils backupUtils, @NonNull TemporaryRepository tempRepo) {
        this.backupUtils = backupUtils;
        this.tempRepo = tempRepo;

        counterDisposable.addAll(
                tempRepo
                        .getAccounts()
                        .subscribeOn(Schedulers.io())
                        .map(tuples -> {
                            ResultSetIterator<Tuple> it = (ResultSetIterator<Tuple>) tuples.iterator();
                            int cnt = it.unwrap(Cursor.class).getCount();
                            int selected = 0;
                            while (it.hasNext()) {
                                Tuple tuple = it.next();
                                if (tuple.get(TempAccount.RESTORE)) {
                                    selected++;
                                }
                            }
                            it.close();

                            return new Pair<>(selected, cnt);
                        })
                        .subscribe(pair -> {
                            //noinspection ConstantConditions
                            accountSelected.set(pair.first);
                            //noinspection ConstantConditions
                            accountTotal.set(pair.second);
                        }),
                tempRepo
                        .getLabels()
                        .subscribeOn(Schedulers.io())
                        .map(tuples -> {
                            ResultSetIterator<Tuple> it = (ResultSetIterator<Tuple>) tuples.iterator();
                            int cnt = it.unwrap(Cursor.class).getCount();
                            int selected = 0;
                            while (it.hasNext()) {
                                Tuple tuple = it.next();
                                if (tuple.get(TempLabel.RESTORE)) {
                                    selected++;
                                }
                            }
                            it.close();

                            return new Pair<>(selected, cnt);
                        })
                        .subscribe(pair -> {
                            //noinspection ConstantConditions
                            labelSelected.set(pair.first);
                            //noinspection ConstantConditions
                            labelTotal.set(pair.second);
                        })
        );

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
        counterDisposable.dispose();
        cancelRestore();
    }

    @CheckResult
    Observable<SingleEvent> startRestore(@NonNull Bundle bundle) {
        /*if (restart && (restoreSubject.hasThrowable() || restoreSubject.hasComplete())) {
            uri = null;
            backupFile = null;
            compositeDisposable.clear();
            restoreSubject = BehaviorSubject.create();
        }*/

        if (uri != null) {
            return restoreSubject.hide();
        }

        restoreInProgress = null;
        processingFile.set(true);

        RestoreFragmentArgs args = RestoreFragmentArgs.fromBundle(bundle);
        uri = args.getUri();

        compositeDisposable.add(
                backupUtils.restore(uri)
                        .prepare()
                        .observeOn(Schedulers.newThread())
                        .subscribe(restore -> {
                            restoreInProgress = restore;
                            if (restore.isPasswordNeeded()) {
                                restoreSubject.onNext(new SingleEvent<>(ACTION_RESTORE_PASSWORD_NEEDED));
                            } else {
                                continueRestore(null);
                            }
                        }, throwable -> {
                            Timber.e(throwable, "Cannot restore: %s", throwable.getMessage());
                            // TODO
                            if (throwable instanceof ZipException) {
                                int code = ((ZipException) throwable).getCode();
                                switch (code) {
                                    case ZipExceptionConstants.notZipFile:
                                        errorMsg.set(R.string.restore_error_invalid_zip);
                                        break;
                                    default:
                                        errorMsg.set(R.string.default_error_msg);
                                }
                            } else {
                                errorMsg.set(R.string.default_error_msg);
                            }
                            processingFile.set(false);
                            //Snackbar.make(getView(), "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                        })
        );

        return restoreSubject.hide();
    }

    void continueRestore(@Nullable char[] password) {
        if (restoreInProgress == null) {
            throw new IllegalStateException("Restore is not in progress");
        }

        compositeDisposable.clear();

        compositeDisposable.add(
                restoreInProgress.readDataFile(password)
                        .subscribe((backupMeta) -> {
                            meta.set(backupMeta);
                            restoreSubject.onNext(new SingleEvent<>(ACTION_DATA_LOADED));
                            processingFile.set(false);
                            //restoreSubject.onComplete();
                            //restoreInProgress = null;
                        }, throwable -> {
                            if (throwable instanceof ZipException && ((ZipException) throwable).getCode() == ZipExceptionConstants.WRONG_PASSWORD) {
                                restoreSubject.onNext(new SingleEvent<>(ACTION_RESTORE_WRONG_PASSWORD));
                            } else {
                                if (throwable instanceof NotNinjAuthZipFile) {
                                    errorMsg.set(R.string.restore_error_not_ninjauth);
                                } else {
                                    errorMsg.set(R.string.default_error_msg);
                                }
                                processingFile.set(false);
                                restoreSubject.onError(throwable);
                            }
                        })
        );
    }

    void doRestore() {
        if (restoreInProgress == null) {
            throw new IllegalStateException("Restore is not in progress");
        }

        if (requestCounter.get() > 0) {
            // TODO wait for the DB jobs to finish
        }

        inputEnabled.set(false);

        compositeDisposable.clear();

        compositeDisposable.add(
                restoreInProgress.restore().subscribe(() -> {
                    Timber.v("Restore complete");
                    //restoreSubject.onNext(new SingleEvent<>(ACTION_RESTORE_COMPLETE));
                    restoreSubject.onComplete();
                }, throwable -> {
                    Timber.e(throwable, "Restore failed");
                    restoreSubject.onError(throwable);
                    inputEnabled.set(true);
                })
        );
    }

    void cancelRestore() {
        if (restoreInProgress != null) {
            restoreInProgress.dispose();
            restoreInProgress = null;
        }

        if (!restoreSubject.hasComplete() && !restoreSubject.hasThrowable()) {
            restoreSubject.onError(new UserCanceledRestoreException());
        }
    }

    public CharSequence formatDate(Context context, Date date) {
        return DateUtils.getRelativeDateTimeString(context, date.getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
    }

    public void onRestoreClick(View v) {
        doRestore();
    }
}
