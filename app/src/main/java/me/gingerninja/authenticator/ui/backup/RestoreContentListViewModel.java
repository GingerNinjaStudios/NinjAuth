package me.gingerninja.authenticator.ui.backup;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.requery.query.Tuple;
import io.requery.reactivex.ReactiveResult;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.util.AutoClosingMutableLiveData;

public class RestoreContentListViewModel extends ViewModel {
    private final static Completable POISON = Completable.complete();

    @NonNull
    private final TemporaryRepository repo;

    public ObservableInt title = new ObservableInt();
    public ObservableBoolean hasLoaded = new ObservableBoolean(false);

    private AutoClosingMutableLiveData<ResultSetIterator<Tuple>> data = new AutoClosingMutableLiveData<>();
    private Disposable disposable;

    private RestoreContentListFragment.Type type;

    private AtomicInteger requestCounter = new AtomicInteger(0);
    private LinkedBlockingQueue<Completable> workQueue = new LinkedBlockingQueue<>();
    private Disposable workDisposable;

    @SuppressLint("CheckResult")
    @Inject
    RestoreContentListViewModel(@NonNull TemporaryRepository repo) {
        this.repo = repo;

        workDisposable = Observable
                .<Completable>create(emitter -> {
                    try {
                        while (true) {
                            Completable item = workQueue.take();
                            if (item == POISON) {
                                break;
                            }
                            emitter.onNext(item);
                        }
                    } catch (InterruptedException ignored) {
                    }

                    emitter.onComplete();
                })
                .doOnTerminate(() -> {
                    workQueue.clear();
                    requestCounter.set(0);
                    workDisposable.dispose();
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())
                .subscribe(completable -> {
                    //noinspection ResultOfMethodCallIgnored,ThrowableNotThrown
                    completable.doOnTerminate(() -> requestCounter.decrementAndGet()).blockingGet();
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        if (data.getValue() != null) {
            data.getValue().close();
        }

        workQueue.add(POISON);
    }

    void init(RestoreContentListFragment.Type type) {
        if (this.type != null) {
            return;
        }

        this.type = type;

        switch (type) {
            case ACCOUNTS:
                title.set(R.string.nav_accounts_title);
                getAccounts();
                break;
            case LABELS:
                title.set(R.string.nav_labels_title);
                getLabels();
                break;
        }
    }

    private void dataRetrieved(ReactiveResult<Tuple> tuples) throws Exception {
        ResultSetIterator<Tuple> it = (ResultSetIterator<Tuple>) tuples.iterator();
        hasLoaded.set(true);
        data.postValue(it);
    }

    private void getAccounts() {
        disposable = repo.getAccounts()
                .subscribeOn(Schedulers.io())
                .subscribe(this::dataRetrieved);
    }

    private void getLabels() {
        disposable = repo.getLabels()
                .subscribeOn(Schedulers.io())
                .subscribe(this::dataRetrieved);
    }

    public LiveData<ResultSetIterator<Tuple>> getData() {
        return data;
    }

    /**
     * Handles a database request, such as updating the restore mode or should-restore.
     *
     * @param completable the DB work to be done
     */
    void handleRequest(@NonNull Completable completable) {
        workQueue.add(completable);
        requestCounter.incrementAndGet();
    }
}
