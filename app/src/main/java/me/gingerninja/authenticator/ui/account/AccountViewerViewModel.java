package me.gingerninja.authenticator.ui.account;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;

public class AccountViewerViewModel extends BaseAccountViewModel {
    @Inject
    AccountViewerViewModel(@NonNull AccountRepository accountRepository) {
        super(accountRepository);
    }

    @Override
    protected long getIdFromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            return 0;
        } else {
            return AccountViewerFragmentArgs.fromBundle(bundle).getId();
        }
    }

    @NonNull
    @Override
    protected Account createAccount(@Nullable Bundle bundle) {
        throw new UnsupportedOperationException("Cannot create account in view mode");
    }
}
