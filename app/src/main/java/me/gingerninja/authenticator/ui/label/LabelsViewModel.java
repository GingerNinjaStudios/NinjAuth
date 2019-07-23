package me.gingerninja.authenticator.ui.label;

import android.database.Cursor;
import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.AutoClosingMutableLiveData;
import me.gingerninja.authenticator.util.SingleEvent;

public class LabelsViewModel extends ViewModel {
    static final String NAV_ADD_LABEL = "nav.addLabel";

    private final AccountRepository accountRepo;

    public ObservableBoolean hasLoaded = new ObservableBoolean(false);
    public ObservableBoolean hasData = new ObservableBoolean(false);

    public ObservableInt menuRes = new ObservableInt(R.menu.label_list_menu);

    private MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();
    //private MutableLiveData<List<Label>> labelList = new MutableLiveData<>();

    private AutoClosingMutableLiveData<ResultSetIterator<Label>> labelList2 = new AutoClosingMutableLiveData<>();

    public ObservableBoolean isOrdering = new ObservableBoolean(false);
    public ObservableInt headerMessage = new ObservableInt(0);

    private MutableLiveData<Boolean> isOrderingLive = new MutableLiveData<>(false);

    private Disposable disposable;

    @Inject
    LabelsViewModel(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;

        disposable = accountRepo
                .getAllLabelAndListen2()
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe(labels -> {
                    ResultSetIterator<Label> it = (ResultSetIterator<Label>) labels.iterator();
                    hasLoaded.set(true);
                    hasData.set(it.unwrap(Cursor.class).getCount() > 0);
                    labelList2.postValue(it);
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        if (labelList2.getValue() != null) {
            labelList2.getValue().close();
        }
    }

    public void onAddLabelClick(View view) {
        if (isOrdering.get()) {
            setReorderingEnabled(false);
        } else {
            navAction.setValue(new SingleEvent<>(NAV_ADD_LABEL));
        }
    }

    LiveData<ResultSetIterator<Label>> getLabelList() {
        return labelList2;
    }

    LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }

    LiveData<Boolean> getIsOrdering() {
        return isOrderingLive;
    }

    void saveListOrder(int count, int[] movement) {
        if (movement[0] == movement[1]) {
            return;
        }

        accountRepo.saveLabelOrder(count, movement[0], movement[1], labelList2.getValue()).subscribe();
    }

    void setReorderingEnabled(boolean enabled) {
        /*if (enabled) {
            setFilterAndRetrieve(null);
        }*/

        isOrderingLive.setValue(enabled);
        isOrdering.set(enabled);
        menuRes.set(enabled ? 0 : R.menu.label_list_menu);
        updateHeaderMessage();
    }

    private void updateHeaderMessage() {
        /*if (hasFilter.get()) {
            headerMessage.set(R.string.filters_active_msg);
        } else*/
        if (isOrdering.get()) {
            headerMessage.set(R.string.reorder_active_msg);
        } else {
            headerMessage.set(0);
        }
    }
}
