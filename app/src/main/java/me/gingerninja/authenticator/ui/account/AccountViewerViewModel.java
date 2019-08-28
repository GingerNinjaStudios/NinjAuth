package me.gingerninja.authenticator.ui.account;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.wrapper.AccountDataProjector;
import me.gingerninja.authenticator.data.repo.AccountRepository;

public class AccountViewerViewModel extends BaseAccountViewModel {
    @NonNull
    private final Context context;

    @Inject
    AccountViewerViewModel(@NonNull Context context, @NonNull AccountRepository accountRepository) {
        super(accountRepository);
        this.context = context;
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

    @Override
    protected Data createData() {
        return new ViewData(context);
    }

    public static class ViewData extends Data {
        private final Context context; // this is the application context, nothing to worry about

        private ViewData(Context context) {
            this.context = context;
        }

        @Override
        protected void init(@NonNull Account account) {
            super.init(account);

            AccountDataProjector projector = new AccountDataProjector(account);
            this.type.set(context.getString(projector.getTypeAsResource()));
            this.typeSpecificData.set(projector.getTypeSpecificDataAsReadable(context));
            this.algorithm.set(projector.getAlgorithmAsReadable());
        }
    }
}
