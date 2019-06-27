package me.gingerninja.authenticator.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

import javax.inject.Inject;

import me.gingerninja.authenticator.data.repo.AccountRepository;
import timber.log.Timber;

public class DeleteAccountViewModel extends ViewModel {
    static final String ACTION_CANCEL = "cancel";
    static final String ACTION_DELETE = "delete";

    private long accountId;

    @NonNull
    private String accountTitle;

    @NonNull
    private String accountName;

    @Nullable
    private String accountIssuer;

    private AccountRepository accountRepository;

    private MutableLiveData<String> action = new MutableLiveData<>();

    @Inject
    public DeleteAccountViewModel(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    void setData(@NonNull Bundle args) {
        accountId = args.getLong(DeleteAccountBottomFragment.ARG_ACCOUNT_ID);
        accountTitle = Objects.requireNonNull(args.getString(DeleteAccountBottomFragment.ARG_ACCOUNT_TITLE));
        accountName = Objects.requireNonNull(args.getString(DeleteAccountBottomFragment.ARG_ACCOUNT_NAME));
        accountIssuer = args.getString(DeleteAccountBottomFragment.ARG_ACCOUNT_ISSUER);
    }

    @NonNull
    public String getAccountTitle() {
        return accountTitle;
    }

    @NonNull
    public String getAccountName() {
        return accountName;
    }

    @Nullable
    public String getAccountIssuer() {
        return accountIssuer;
    }

    public void onCancelClick(View view) {
        action.setValue(ACTION_CANCEL);
    }

    public void onDeleteClick(View view) {
        Timber.d("Account ID: %d", accountId);
        accountRepository
                .deleteAccount(accountId)
                .subscribe(() -> action.postValue(ACTION_DELETE), throwable -> {
                    Timber.e(throwable, "Cannot delete account");
                    // TODO
                });
    }

    public LiveData<String> getAction() {
        return action;
    }
}
