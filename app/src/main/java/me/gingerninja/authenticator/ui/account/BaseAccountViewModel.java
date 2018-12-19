package me.gingerninja.authenticator.ui.account;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;
import me.gingerninja.authenticator.BR;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;

public abstract class BaseAccountViewModel extends ViewModel {
    protected Account account;
    public Data data;

    public ObservableInt typeSpecificTitle = new ObservableInt();
    public ObservableInt typeSpecificDesc = new ObservableInt();

    private final Observable.OnPropertyChangedCallback typeChangeCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            changeTypeSpecificDetails(data.type.get());
        }
    };

    @Override
    protected void onCleared() {
        super.onCleared();

        if (data != null && data.type != null) {
            data.type.removeOnPropertyChangedCallback(typeChangeCallback);
        }
    }

    public void init(@Nullable Bundle bundle) {
        if (account != null) {
            return;
        }

        account = initAccount(bundle);
        initFieldsFromAccount();

        changeTypeSpecificDetails(data.type.get());
        data.type.addOnPropertyChangedCallback(typeChangeCallback);
    }

    private void changeTypeSpecificDetails(@Nullable String type) {
        if (type == null) {
            typeSpecificTitle.set(0);
            typeSpecificDesc.set(0);
        } else {
            switch (type) {
                case Account.TYPE_TOTP:
                    typeSpecificTitle.set(R.string.account_type_spec_totp_text);
                    typeSpecificDesc.set(R.string.account_type_spec_totp_desc);
                    break;
                case Account.TYPE_HOTP:
                    typeSpecificTitle.set(R.string.account_type_spec_hotp_text);
                    typeSpecificDesc.set(R.string.account_type_spec_hotp_desc);
                    break;
                default:
                    typeSpecificTitle.set(0);
                    typeSpecificDesc.set(0);
            }
        }
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
