package me.gingerninja.authenticator.ui.backup.page;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.requery.query.Tuple;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.util.AutoClosingMutableLiveData;

public class RestoreSummaryPageViewModel extends ViewModel {

    private final TemporaryRepository repo;

    private Disposable disposable;
    private AutoClosingMutableLiveData<ResultSetIterator<Tuple>> labels = new AutoClosingMutableLiveData<>();

    @Inject
    RestoreSummaryPageViewModel(TemporaryRepository repo) {
        this.repo = repo;

        disposable = repo.getLabels()
                .subscribeOn(Schedulers.io())
                .subscribe(tuples -> {
                    ResultSetIterator<Tuple> it = (ResultSetIterator<Tuple>) tuples.iterator();
                    //hasLoaded.set(true);
                    //hasData.set(it.unwrap(Cursor.class).getCount() > 0);
                    labels.postValue(it);
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        labels.close();
    }

    LiveData<ResultSetIterator<Tuple>> getLabels() {
        return labels;
    }
}
