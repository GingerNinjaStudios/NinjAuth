package me.gingerninja.authenticator.ui.home;

import android.app.Application;
import android.database.Cursor;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.requery.query.Tuple;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.ui.home.filter.AccountFilterObject;
import me.gingerninja.authenticator.util.AutoClosingMutableLiveData;
import me.gingerninja.authenticator.util.SingleEvent;

public class AccountListViewModel extends ViewModel {
    public static final String NAV_ADD_ACCOUNT_FROM_CAMERA = "nav.addAccountFromCamera";

    private final Application application;
    private final AccountRepository accountRepo;
    public ObservableBoolean hasLoaded = new ObservableBoolean(false);
    public ObservableBoolean hasData = new ObservableBoolean(false);
    private MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();
    //private MutableLiveData<List<Account>> accountList = new MutableLiveData<>();
    private AutoClosingMutableLiveData<ResultSetIterator<Tuple>> accountList2 = new AutoClosingMutableLiveData<>();
    private Disposable disposable;

    public ObservableBoolean hasFilter = new ObservableBoolean(false);
    public ObservableBoolean isOrdering = new ObservableBoolean(false);
    public ObservableInt menuRes = new ObservableInt(R.menu.account_list_menu);
    public ObservableInt headerMessage = new ObservableInt(0);

    private MutableLiveData<Boolean> isOrderingLive = new MutableLiveData<>(false);

    @Nullable
    private AccountFilterObject filterObject;

    @Inject
    public AccountListViewModel(Application application, AccountRepository accountRepo) {
        this.application = application;
        this.accountRepo = accountRepo;

        setFilterAndRetrieve(null);
        /*disposable = accountRepo.getAllAccountAndListen2(null)
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe(tuples -> {
                    ResultSetIterator<Tuple> it = (ResultSetIterator<Tuple>) tuples.iterator();
                    hasLoaded.set(true);
                    hasData.set(it.unwrap(Cursor.class).getCount() > 0);
                    accountList2.postValue(it);
                });*/

        /*disposable = accountRepo.getAllAccountAndListen()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accounts -> {
                    accountList.postValue(accounts);
                });*/
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        if (accountList2.getValue() != null) {
            accountList2.getValue().close();
        }
    }

    @Nullable
    public AccountFilterObject getFilterObject() {
        return filterObject;
    }

    public void setFilterAndRetrieve(@Nullable AccountFilterObject filterObject) {
        this.filterObject = filterObject;

        hasFilter.set(filterObject != null && filterObject.hasFilter());

        updateHeaderMessage();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        disposable = accountRepo.getAllAccountAndListen2(filterObject)
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe(tuples -> {
                    /*ResultSetIterator<Tuple> oldResults = accountList2.getValue();

                    // if there are active observers, we will let them close the result set
                    if (oldResults != null && !accountList2.hasActiveObservers()) {
                        oldResults.close();
                    }*/
                    ResultSetIterator<Tuple> it = (ResultSetIterator<Tuple>) tuples.iterator();
                    hasLoaded.set(true);
                    hasData.set(it.unwrap(Cursor.class).getCount() > 0);
                    accountList2.postValue(it);
                });
    }

    private void updateHeaderMessage() {
        if (hasFilter.get()) {
            headerMessage.set(R.string.filters_active_msg);
        } else if (isOrdering.get()) {
            headerMessage.set(R.string.reorder_active_msg);
        } else {
            headerMessage.set(0);
        }
    }

    /*LiveData<List<Account>> getAccountList() {
        return accountList;
    }*/

    LiveData<ResultSetIterator<Tuple>> getAccountList2() {
        return accountList2;
    }

    LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }

    LiveData<Boolean> getIsOrdering() {
        return isOrderingLive;
    }

    /*void saveList(List<Account> accounts) {
        accountRepo.saveAccounts(accounts).subscribe();
    }*/

    void saveListOrder(int count, int[] movement) {
        if (movement[0] == movement[1]) {
            return;
        }

        accountRepo.saveAccountOrder(count, movement[0], movement[1], accountList2.getValue()).subscribe();
    }

    public void onAddAccountFromCameraClick(View view) {
        if (isOrdering.get()) {
            setReorderingEnabled(false);
        } else {
            navAction.setValue(new SingleEvent<>(NAV_ADD_ACCOUNT_FROM_CAMERA));
        }
        /*if (PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(application, Manifest.permission.CAMERA)) {
            // open camera
        } else {
            // request permission

        }*/
    }

    void setReorderingEnabled(boolean enabled) {
        if (enabled) {
            setFilterAndRetrieve(null);
        }

        isOrderingLive.setValue(enabled);
        isOrdering.set(enabled);
        menuRes.set(enabled ? 0 : R.menu.account_list_menu);
        updateHeaderMessage();
    }
}
