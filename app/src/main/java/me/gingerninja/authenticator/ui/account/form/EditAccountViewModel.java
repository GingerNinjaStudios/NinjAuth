package me.gingerninja.authenticator.ui.account.form;

import android.os.Bundle;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.ui.account.BaseEditableAccountViewModel;

public class EditAccountViewModel extends BaseEditableAccountViewModel {
    @Inject
    EditAccountViewModel(@NonNull AccountRepository accountRepository) {
        super(accountRepository);
    }

    @NonNull
    @Override
    protected Account initAccount(@Nullable Bundle bundle) {
        if (bundle != null) {
            EditAccountFragmentArgs args = EditAccountFragmentArgs.fromBundle(bundle);
            long accountId = args.getId();

            return accountRepository.getAccount(accountId).blockingGet();
        }

        throw new IllegalArgumentException("The bundle must not be null");
    }
}
