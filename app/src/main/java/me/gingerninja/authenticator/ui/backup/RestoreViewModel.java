package me.gingerninja.authenticator.ui.backup;

import android.net.Uri;
import android.os.Bundle;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.exception.ZipExceptionConstants;

import javax.inject.Inject;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import me.gingerninja.authenticator.data.pojo.BackupFile;
import me.gingerninja.authenticator.util.SingleEvent;
import me.gingerninja.authenticator.util.backup.BackupUtils;
import me.gingerninja.authenticator.util.backup.Restore;
import timber.log.Timber;

public class RestoreViewModel extends ViewModel {
    static final String ACTION_RESTORE_PASSWORD_NEEDED = "restore-password-needed";
    static final String ACTION_RESTORE_WRONG_PASSWORD = "restore-wrong-password";

    @NonNull
    private final BackupUtils backupUtils;

    @NonNull
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    private Restore restoreInProgress;

    @Nullable
    private Uri uri;

    @NonNull
    private BehaviorSubject<SingleEvent<BackupFile>> restoreSubject = BehaviorSubject.create();

    @Nullable
    private BackupFile backupFile;

    @Inject
    RestoreViewModel(@NonNull BackupUtils backupUtils) {
        this.backupUtils = backupUtils;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
        cancelRestore();
        backupFile = null;
    }

    @Nullable
    BackupFile getBackupFile() {
        return backupFile;
    }

    @CheckResult
    Observable<SingleEvent<BackupFile>> startRestore(@NonNull Bundle bundle) {
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
                restoreInProgress.restore(password)
                        .subscribe(backupFile -> {
                            this.backupFile = backupFile;
                            restoreSubject.onComplete();

                            restoreInProgress = null;
                        }, throwable -> {
                            if (throwable instanceof ZipException) {
                                int code = ((ZipException) throwable).getCode();

                                if (code == ZipExceptionConstants.WRONG_PASSWORD) {
                                    restoreSubject.onNext(new SingleEvent<>(ACTION_RESTORE_WRONG_PASSWORD));
                                } else {
                                    restoreSubject.onError(throwable);
                                }
                            } else {
                                restoreSubject.onError(throwable);
                            }
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
}
