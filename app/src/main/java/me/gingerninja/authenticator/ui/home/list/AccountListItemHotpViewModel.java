package me.gingerninja.authenticator.ui.home.list;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.CodeGenerator;

public class AccountListItemHotpViewModel extends AccountListItemViewModel {
    @NonNull
    private final AccountRepository accountRepository;

    public AccountListItemHotpViewModel(@NonNull Account account, @NonNull CodeGenerator codeGenerator, @NonNull AccountRepository accountRepository) {
        super(account, codeGenerator);
        this.accountRepository = accountRepository;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void onRefreshButtonClick(View v) {
        accountRepository.getAccount(account.getId())
                .subscribeOn(Schedulers.io())
                .flatMap(acc -> accountRepository.addAccount(acc.setTypeSpecificData(acc.getTypeSpecificData() + 1)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountEntity -> {
                    if (accountEntity != null) {
                        account = accountEntity;
                    }
                }, throwable -> {
                });
        //account.setTypeSpecificData(account.getTypeSpecificData() + 1L);
        //accountRepository.addAccount(account);
    }
}
