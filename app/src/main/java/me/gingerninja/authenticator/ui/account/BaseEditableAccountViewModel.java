package me.gingerninja.authenticator.ui.account;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.SingleEvent;

public abstract class BaseEditableAccountViewModel extends BaseAccountViewModel {
    public static final String NAV_ACTION_SAVE = "account.save";

    @NonNull
    protected AccountRepository accountRepository;

    protected MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();

    protected Disposable saveDisposable;

    public BaseEditableAccountViewModel(@NonNull AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    protected boolean checkValues() {
        return data.prepareAndCheckData(account);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (saveDisposable != null && !saveDisposable.isDisposed()) {
            saveDisposable.dispose();
        }
    }

    public void onSaveClick(View view) {
        if (checkValues()) {
            saveDisposable = accountRepository
                    .addAccount(account)
                    .subscribe(account -> navAction.postValue(new SingleEvent<>(NAV_ACTION_SAVE, account.getTitle())));

        }
    }

    public LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }
}
