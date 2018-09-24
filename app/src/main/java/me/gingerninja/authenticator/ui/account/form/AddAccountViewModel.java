package me.gingerninja.authenticator.ui.account.form;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.Parser;
import me.gingerninja.authenticator.util.SingleEvent;

public class AddAccountViewModel extends ViewModel {
    public static final String NAV_ACTION_SAVE = "addAccount.save";

    @NonNull
    private AccountRepository accountRepository;

    private Account initAccount;

    private MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();

    private Disposable saveDisposable;

    public ObservableField<String> title = new ObservableField<>();

    @Inject
    public AddAccountViewModel(@NonNull AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void onSaveClick(View view) {
        if (prepareAndCheckData()) {
            saveDisposable = accountRepository
                    .addAccount(initAccount)
                    .subscribe(account -> navAction.postValue(new SingleEvent<>(NAV_ACTION_SAVE)));

        }
    }

    private boolean prepareAndCheckData() {
        boolean hasError = false;

        String rawTitle = title.get();
        if (TextUtils.isEmpty(rawTitle) || TextUtils.isEmpty(rawTitle.trim())) {
            hasError = true;
        }

        initAccount.setTitle(title.get());

        return !hasError;
    }

    public LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }

    public void init(Bundle bundle) {
        if (initAccount != null) {
            return;
        }

        if (bundle != null) {
            AddAccountFragmentArgs args = AddAccountFragmentArgs.fromBundle(bundle);

            if (args.getUrl() != null) {
                initAccount = Parser.parseUrl(args.getUrl());
                initFieldsFromAccount(initAccount);
            } else {

            }
        }
    }

    private void initFieldsFromAccount(@Nullable Account account) {
        if (account == null) {
            return;
        }

        title.set(account.getTitle());
    }
}
