package me.gingerninja.authenticator.ui.backup;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.exception.ZipExceptionConstants;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import me.gingerninja.authenticator.util.SingleEvent;
import me.gingerninja.authenticator.util.backup.BackupUtils;
import me.gingerninja.authenticator.util.backup.Restore;
import timber.log.Timber;

public class RestoreViewModel extends ViewModel implements WorkUpdateHandler {
    static final String ACTION_RESTORE_PASSWORD_NEEDED = "restore-password-needed";
    static final String ACTION_RESTORE_WRONG_PASSWORD = "restore-wrong-password";

    static final String ACTION_DATA_LOADED = "restore-data-loaded";
    //static final String ACTION_RESTORE_COMPLETE = "restore-complete";
    @NonNull
    private final BackupUtils backupUtils;
    private AtomicInteger requestCounter = new AtomicInteger(0);
    private LinkedBlockingQueue<Completable> workQueue = new LinkedBlockingQueue<>();

    private Disposable workDisposable;

    @NonNull
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    private Restore restoreInProgress;

    @Nullable
    private Uri uri;

    @NonNull
    private BehaviorSubject<SingleEvent> restoreSubject = BehaviorSubject.create();

    public ObservableBoolean processingFile = new ObservableBoolean(true);

    @SuppressLint("CheckResult")
    @Inject
    RestoreViewModel(@NonNull BackupUtils backupUtils) {
        this.backupUtils = backupUtils;

        workDisposable = Observable
                .<Completable>create(emitter -> {
                    Completable completable;
                    try {
                        //noinspection InfiniteLoopStatement
                        while (true) {
                            emitter.onNext(workQueue.take());
                        }
                    } catch (InterruptedException ignored) {
                    }

                    emitter.onComplete();
                })
                .doOnTerminate(() -> {
                    workQueue.clear();
                    requestCounter.set(0);
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())
                .subscribe(completable -> {
                    //noinspection ThrowableNotThrown,ResultOfMethodCallIgnored
                    completable.doOnTerminate(() -> requestCounter.decrementAndGet()).blockingGet();
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        workDisposable.dispose();
        compositeDisposable.dispose();
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
                        .subscribe(() -> {
                            restoreSubject.onNext(new SingleEvent<>(ACTION_DATA_LOADED));
                            processingFile.set(false);
                            //restoreSubject.onComplete();
                            //restoreInProgress = null;
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

    void doRestore() {
        if (restoreInProgress == null) {
            throw new IllegalStateException("Restore is not in progress");
        }

        if (requestCounter.get() > 0) {
            // TODO wait for the DB jobs to finish
        }

        compositeDisposable.clear();

        compositeDisposable.add(
                restoreInProgress.restore().subscribe(() -> {
                    Timber.v("Restore complete");
                    //restoreSubject.onNext(new SingleEvent<>(ACTION_RESTORE_COMPLETE));
                    restoreSubject.onComplete();
                }, throwable -> {
                    Timber.e(throwable, "Restore failed");
                    restoreSubject.onError(throwable);
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

    /**
     * Handles a database request, such as updating the restore mode or should-restore.
     *
     * @param completable
     */
    public void handleRequest(@NonNull Completable completable) {
        workQueue.add(completable);
        requestCounter.incrementAndGet();
        /*completable
                .observeOn(Schedulers.single())
                .doOnTerminate(() -> requestCounter.decrementAndGet())
                .subscribe();*/
    }
}
