package me.gingerninja.authenticator.ui.home.form;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.wrapper.AccountDataProjector;

public class ExistingAccountViewModel extends ViewModel {
    public ObservableBoolean showComparison = new ObservableBoolean(false);

    public AccountDataProjector oldAccount, newAccount;

    @Inject
    ExistingAccountViewModel() {
    }

    void setAccounts(@NonNull Account oldAccount, @NonNull Account newAccount) {
        if (this.oldAccount != null) {
            return;
        }

        this.oldAccount = new AccountDataProjector(oldAccount);
        this.newAccount = new AccountDataProjector(newAccount);
    }

    @SuppressWarnings("unused")
    public void onShowComparisonClick(View ignored) {
        showComparison.set(true);
    }

    public boolean sameSecrets() {
        return TextUtils.equals(oldAccount.getSecret(), newAccount.getSecret());
    }
}
