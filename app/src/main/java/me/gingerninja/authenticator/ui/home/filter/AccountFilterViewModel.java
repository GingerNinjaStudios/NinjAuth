package me.gingerninja.authenticator.ui.home.filter;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.repo.AccountRepository;

public class AccountFilterViewModel extends ViewModel {
    @NonNull
    private final AccountRepository repository;
    //AutoClosingMutableLiveData<ResultSetIterator<Label>> resultLiveData = new AutoClosingMutableLiveData<>();
    private MutableLiveData<List<Label>> labels = new MutableLiveData<>();

    private HashSet<Label> filterLabels = new HashSet<>();
    private MutableLiveData<HashSet<Label>> filterLabelLiveData = new MutableLiveData<>(filterLabels);

    private CompositeDisposable disposable = new CompositeDisposable();
    private CompositeDisposable filterDisposable = new CompositeDisposable();

    public ObservableField<String> searchStringInput = new ObservableField<>("");
    public ObservableBoolean isFiltering = new ObservableBoolean(false);
    public ObservableInt resultCounter = new ObservableInt(-1);

    private String searchString = "";

    private PublishSubject<String> searchSubject = PublishSubject.create();

    private Observable.OnPropertyChangedCallback searchStringCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            //noinspection ConstantConditions
            searchSubject.onNext(searchStringInput.get());
        }
    };

    @Inject
    AccountFilterViewModel(@NonNull AccountRepository repository) {
        this.repository = repository;

        disposable.addAll(
                searchSubject
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(str -> {
                            searchString = str;
                            refreshUi();
                            findAccounts();
                        }),
                repository
                        .getAllLabelAndListen()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.labels::setValue)
        );

        searchStringInput.addOnPropertyChangedCallback(searchStringCallback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        searchStringInput.removeOnPropertyChangedCallback(searchStringCallback);
        disposable.dispose();
    }

    @NonNull
    LiveData<HashSet<Label>> getFilterLabels() {
        return filterLabelLiveData;
    }

    void addLabelToFilter(@NonNull Label label) {
        filterLabels.add(label);
        refreshUi();
        findAccounts();
    }

    void removeLabelFromFilter(@NonNull Label label) {
        filterLabels.remove(label);
        refreshUi();
        findAccounts();
    }

    public void onResetClick(View view) {
        searchString = "";
        searchStringInput.removeOnPropertyChangedCallback(searchStringCallback);
        searchStringInput.set("");
        searchStringInput.addOnPropertyChangedCallback(searchStringCallback);
        filterLabels.clear();
        filterLabelLiveData.setValue(filterLabels);
        refreshUi();
        findAccounts();
    }

    public boolean onSearchInit(TextView view, int actionId, KeyEvent event) {
        // TODO dismiss the dialog and show results
        return true;
    }

    private void refreshUi() {
        isFiltering.set(!filterLabels.isEmpty() || (!TextUtils.isEmpty(searchStringInput.get()) && !TextUtils.isEmpty(searchStringInput.get().trim())));
    }

    private void findAccounts() {
        filterDisposable.clear();

        if (isFiltering.get()) {
            AccountFilterObject filterObject = new AccountFilterObject.Builder().setLabels(filterLabels).setSearchString(searchString).build();

            filterDisposable.add(
                    repository
                            .getFilteredAccountCount(filterObject)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(resultCounter::set)
            );
        } else {
            resultCounter.set(-1);
        }
    }

    public LiveData<List<Label>> getLabels() {
        return labels;
    }
}
