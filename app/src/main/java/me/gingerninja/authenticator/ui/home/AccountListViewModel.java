package me.gingerninja.authenticator.ui.home;

import android.app.Application;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.SingleEvent;

public class AccountListViewModel extends ViewModel {
    public static final String NAV_ADD_ACCOUNT_FROM_CAMERA = "nav.addAccountFromCamera";

    private final Application application;
    private final AccountRepository accountRepo;

    private MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();
    private MutableLiveData<List<Account>> accountList = new MutableLiveData<>();

    private Disposable disposable;

    @Inject
    public AccountListViewModel(Application application, AccountRepository accountRepo) {
        this.application = application;
        this.accountRepo = accountRepo;

        disposable = accountRepo.getAllAccountAndListen()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accounts -> {
                    accountList.postValue(accounts);
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    LiveData<List<Account>> getAccountList() {
        return accountList;
    }

    LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }

    void saveList(List<Account> accounts) {
        accountRepo.saveAccounts(accounts).subscribe();
    }

    public void onAddAccountFromCameraClick(View view) {
        navAction.setValue(new SingleEvent<>(NAV_ADD_ACCOUNT_FROM_CAMERA));
        /*if (PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(application, Manifest.permission.CAMERA)) {
            // open camera
        } else {
            // request permission

        }*/
    }
}
