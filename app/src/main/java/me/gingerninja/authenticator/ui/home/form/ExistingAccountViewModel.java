package me.gingerninja.authenticator.ui.home.form;

import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import me.gingerninja.authenticator.data.db.entity.Account;

public class ExistingAccountViewModel extends ViewModel {
    public ObservableBoolean showComparison = new ObservableBoolean(false);

    public Account oldAccount, newAccount;

    @Inject
    ExistingAccountViewModel() {
    }

    void setAccounts(Account oldAccount, Account newAccount) {
        if (oldAccount != null) {
            return;
        }

        this.oldAccount = oldAccount;
        this.newAccount = newAccount;
    }

    @SuppressWarnings("unused")
    public void onShowComparisonClick(View ignored) {
        showComparison.set(true);
    }
}
