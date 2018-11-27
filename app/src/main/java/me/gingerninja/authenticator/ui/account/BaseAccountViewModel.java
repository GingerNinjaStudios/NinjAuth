package me.gingerninja.authenticator.ui.account;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;
import me.gingerninja.authenticator.BR;
import me.gingerninja.authenticator.data.db.entity.Account;

public abstract class BaseAccountViewModel extends ViewModel {
    protected Account account;
    public Data data;

    public void init(@Nullable Bundle bundle) {
        if (account != null) {
            return;
        }

        account = initAccount(bundle);
        initFieldsFromAccount();
    }

    @NonNull
    protected abstract Account initAccount(@Nullable Bundle bundle);

    protected Data createData() {
        return new Data();
    }

    private void initFieldsFromAccount() {
        if (account == null) {
            return;
        }

        data = createData();
        data.init(account);
    }

    public static class Data extends BaseObservable {
        public ObservableField<String> title = new ObservableField<>();
        public ObservableField<String> accountName = new ObservableField<>();
        public ObservableField<String> issuer = new ObservableField<>();
        public ObservableField<String> digits = new ObservableField<>();
        public ObservableField<String> typeSpecificData = new ObservableField<>(); // period for TOTP and counter for HOTP
        public ObservableField<String> secret = new ObservableField<>();

        public ObservableField<String> type = new ObservableField<>();
        public ObservableField<String> algorithm = new ObservableField<>();

        private String source;

        private void init(@NonNull Account account) {
            title.set(account.getTitle());
            accountName.set(account.getAccountName());
            issuer.set(account.getIssuer());
            secret.set(account.getSecret());

            digits.set(Integer.toString(account.getDigits()));
            typeSpecificData.set(Long.toString(account.getTypeSpecificData()));

            type.set(account.getType());
            algorithm.set(account.getAlgorithm());

            source = account.getSource();
            notifyPropertyChanged(BR.source);
        }

        @Bindable
        public String getSource() {
            return source;
        }
    }
}
