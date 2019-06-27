package me.gingerninja.authenticator.ui.backup.page;

import android.database.Cursor;

import androidx.databinding.ObservableLong;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.requery.query.Tuple;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.data.db.entity.TempLabel;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.util.AutoClosingMutableLiveData;

public class RestoreSummaryPageViewModel extends ViewModel {

    private final TemporaryRepository repo;

    private CompositeDisposable disposable = new CompositeDisposable();

    private AutoClosingMutableLiveData<ResultSetIterator<Tuple>> accounts = new AutoClosingMutableLiveData<>();
    private AutoClosingMutableLiveData<ResultSetIterator<Tuple>> labels = new AutoClosingMutableLiveData<>();

    public ObservableLong accountCount = new ObservableLong(0);
    public ObservableLong labelCount = new ObservableLong(0);

    @Inject
    RestoreSummaryPageViewModel(TemporaryRepository repo) {
        this.repo = repo;

        disposable.add(
                repo.getAccounts()
                        .subscribeOn(Schedulers.io())
                        .subscribe(tuples -> {
                            ResultSetIterator<Tuple> it = (ResultSetIterator<Tuple>) tuples.iterator();
                            //hasLoaded.set(true);
                            //hasData.set(it.unwrap(Cursor.class).getCount() > 0);

                            Disposable d = Observable
                                    .<Boolean>create(emitter -> {
                                        try {
                                            Cursor c = it.unwrap(Cursor.class);
                                            int n = c.getCount();

                                            for (int i = 0; i < n; i++) {
                                                emitter.onNext(it.get(i).get(TempLabel.RESTORE));
                                            }

                                            emitter.onComplete();
                                        } catch (Throwable t) {
                                            emitter.onError(t);
                                        }
                                    })
                                    .filter(aBoolean -> aBoolean)
                                    .count()
                                    .subscribe((count, throwable) -> {
                                        if (throwable == null) {
                                            accountCount.set(count);
                                        }
                                    });

                            accounts.postValue(it);
                        })
        );

        disposable.add(
                repo.getLabels()
                        .subscribeOn(Schedulers.io())
                        .subscribe(tuples -> {
                            ResultSetIterator<Tuple> it = (ResultSetIterator<Tuple>) tuples.iterator();
                            //hasLoaded.set(true);
                            //hasData.set(it.unwrap(Cursor.class).getCount() > 0);

                            Disposable d = Observable
                                    .<Boolean>create(emitter -> {
                                        try {
                                            Cursor c = it.unwrap(Cursor.class);
                                            int n = c.getCount();

                                            for (int i = 0; i < n; i++) {
                                                emitter.onNext(it.get(i).get(TempLabel.RESTORE));
                                            }

                                            emitter.onComplete();
                                        } catch (Throwable t) {
                                            emitter.onError(t);
                                        }
                                    })
                                    .filter(aBoolean -> aBoolean)
                                    .count()
                                    .subscribe((count, throwable) -> {
                                        if (throwable == null) {
                                            labelCount.set(count);
                                        }
                                    });

                            labels.postValue(it);
                        })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (!disposable.isDisposed()) {
            disposable.clear();
            disposable.dispose();
        }

        accounts.close();
        labels.close();
    }

    /*LiveData<ResultSetIterator<Tuple>> getLabels() {
        return labels;
    }*/
}
