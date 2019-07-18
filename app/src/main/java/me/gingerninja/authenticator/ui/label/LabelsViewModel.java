package me.gingerninja.authenticator.ui.label;

import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.SingleEvent;

public class LabelsViewModel extends ViewModel {
    static final String NAV_ADD_LABEL = "nav.addLabel";

    public ObservableBoolean hasLoaded = new ObservableBoolean(false);
    public ObservableBoolean hasData = new ObservableBoolean(false);

    private MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();
    private MutableLiveData<List<Label>> labelList = new MutableLiveData<>();

    private Disposable disposable;

    @Inject
    LabelsViewModel(AccountRepository accountRepo) {
        disposable = accountRepo
                .getAllLabelAndListen()
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe(accounts -> {
                    hasLoaded.set(true);
                    hasData.set(!accounts.isEmpty());
                    labelList.postValue(accounts);
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void onAddLabelClick(View view) {
        navAction.setValue(new SingleEvent<>(NAV_ADD_LABEL));
    }

    LiveData<List<Label>> getLabelList() {
        return labelList;
    }

    LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }
}
