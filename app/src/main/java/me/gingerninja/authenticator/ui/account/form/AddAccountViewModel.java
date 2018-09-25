package me.gingerninja.authenticator.ui.account.form;

import android.os.Bundle;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.ui.account.BaseEditableAccountViewModel;
import me.gingerninja.authenticator.util.Parser;

public class AddAccountViewModel extends BaseEditableAccountViewModel {
    @Inject
    AddAccountViewModel(@NonNull AccountRepository accountRepository) {
        super(accountRepository);
    }

    @NonNull
    @Override
    protected Account initAccount(@Nullable Bundle bundle) {
        Account account;

        if (bundle != null) {
            AddAccountFragmentArgs args = AddAccountFragmentArgs.fromBundle(bundle);
            if (args.getUrl() != null) {
                account = Parser.parseUrl(args.getUrl());
            } else {
                account = new Account();
            }
        } else {
            account = new Account();
        }

        // Parser.parseUrl(...) _could_ return null, but not in this case
        //noinspection ConstantConditions
        return account;
    }
}
