package me.gingerninja.authenticator.ui.home.filter;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import me.gingerninja.authenticator.util.SingleEvent;

public class AccountFilterViewModel extends ViewModel {
    public static final String EVENT_FILTER_AND_DISMISS = "dismiss.with.filter";

    @NonNull
    private final AccountRepository repository;
    //AutoClosingMutableLiveData<ResultSetIterator<Label>> resultLiveData = new AutoClosingMutableLiveData<>();
    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();
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

    private boolean initialDataSet = false;
    private AccountFilterObject filterObject;

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
                        .debounce(300, TimeUnit.MILLISECONDS)
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

    void setFilterObject(@Nullable AccountFilterObject filterObject) {
        if (initialDataSet) {
            return;
        }

        initialDataSet = true;
        this.filterObject = filterObject;

        if (filterObject != null) {
            if (filterObject.hasSearchString()) {
                searchString = filterObject.getSearchString(false);
                searchStringInput.set(filterObject.getSearchString(false));
            }

            if (filterObject.hasLabels()) {
                filterLabels = (HashSet<Label>) filterObject.getLabels();
                filterLabelLiveData.setValue(filterLabels);
            }
        }

        refreshUi();
        findAccounts();
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
        boolean enter = event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER);

        //noinspection ConstantConditions
        if ((actionId == EditorInfo.IME_ACTION_SEARCH || enter) && !searchStringInput.get().trim().isEmpty()) {
            // TODO dismiss and search for accounts
            events.setValue(new SingleEvent(EVENT_FILTER_AND_DISMISS));
        }

        return true;
    }

    private void refreshUi() {
        isFiltering.set(!filterLabels.isEmpty() || (!TextUtils.isEmpty(searchStringInput.get()) && !TextUtils.isEmpty(searchStringInput.get().trim())));
    }

    private void findAccounts() {
        filterDisposable.clear();

        if (isFiltering.get()) {
            filterObject = new AccountFilterObject.Builder().setLabels(filterLabels).setSearchString(searchString).build();

            filterDisposable.add(
                    repository
                            .getFilteredAccountCount(filterObject)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(resultCounter::set)
            );
        } else {
            resultCounter.set(-1);
            filterObject = null;
        }
    }

    public LiveData<List<Label>> getLabels() {
        return labels;
    }

    public LiveData<SingleEvent> getEvents() {
        return events;
    }

    @Nullable
    public AccountFilterObject getFilterObject() {
        return filterObject;
    }
}
