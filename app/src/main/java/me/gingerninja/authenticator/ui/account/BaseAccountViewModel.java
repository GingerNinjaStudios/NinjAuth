package me.gingerninja.authenticator.ui.account;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;
import io.reactivex.functions.Function;
import me.gingerninja.authenticator.data.db.entity.Account;
import timber.log.Timber;

public abstract class BaseAccountViewModel extends ViewModel {
    protected Account account;
    public Data data = new Data();

    public void init(@Nullable Bundle bundle) {
        if (account != null) {
            return;
        }

        account = initAccount(bundle);
        initFieldsFromAccount();
    }

    @NonNull
    protected abstract Account initAccount(@Nullable Bundle bundle);

    private void initFieldsFromAccount() {
        if (account == null) {
            return;
        }

        data.init(account);
    }

    public static class Data {
        public ObservableField<String> title = new ObservableField<>();
        public ObservableField<String> accountName = new ObservableField<>();
        public ObservableField<String> issuer = new ObservableField<>();
        public ObservableField<String> digits = new ObservableField<>();
        public ObservableField<String> typeSpecificData = new ObservableField<>(); // period for TOTP and counter for HOTP
        public ObservableField<String> secret = new ObservableField<>();

        public ObservableField<String> type = new ObservableField<>();
        public ObservableField<String> algorithm = new ObservableField<>();

        private void init(@NonNull Account account) {
            title.set(account.getTitle());
            accountName.set(account.getAccountName());
            issuer.set(account.getIssuer());
            secret.set(account.getSecret());

            digits.set(Integer.toString(account.getDigits()));
            typeSpecificData.set(Long.toString(account.getTypeSpecificData()));

            type.set(account.getType());
            algorithm.set(account.getAlgorithm());
        }

        protected boolean prepareAndCheckData(@NonNull Account account) {
            Timber.v("Checking account data: %s", account);
            boolean hasError;

            final String rawTitle = trim(title.get());
            final String rawAccountName = trim(accountName.get());
            final String rawIssuer = trim(issuer.get());
            final String rawDigits = trim(digits.get());
            final String rawTypeSpecificData = trim(typeSpecificData.get());
            final String rawSecret = trim(secret.get());

            final String rawType = type.get();
            final String rawAlgo = algorithm.get();

            hasError = !checkAndSetStringData(rawTitle, account::setTitle);
            hasError |= !checkAndSetStringData(rawAccountName, account::setAccountName);
            //hasError |= !checkAndSetStringData(rawIssuer, account::setIssuer);
            hasError |= !checkAndSetStringData(rawSecret, account::setSecret);

            account.setIssuer(rawIssuer);

            Timber.d("Has error so far: %s", hasError);

            if (!TextUtils.isEmpty(rawDigits) && TextUtils.isDigitsOnly(rawDigits)) {
                account.setDigits(Integer.parseInt(rawDigits));
            } else {
                Timber.d("Digits are empty");
                hasError = true;
            }

            if (!TextUtils.isEmpty(rawType) && !TextUtils.isEmpty(rawTypeSpecificData) && TextUtils.isDigitsOnly(rawTypeSpecificData)) {
                long parsedData = Long.parseLong(rawTypeSpecificData);
                switch (rawType) {
                    case Account.TYPE_TOTP:
                    case Account.TYPE_HOTP:
                        account.setTypeSpecificData(parsedData);
                        break;
                }
            } else {
                Timber.d("Type is empty");
                hasError = true;
            }

            if (!TextUtils.isEmpty(rawAlgo)) {
                switch (rawAlgo) {
                    case Account.ALGO_SHA1:
                        account.setAlgorithm(Account.ALGO_SHA1);
                        break;
                    case Account.ALGO_SHA256:
                        account.setAlgorithm(Account.ALGO_SHA256);
                        break;
                    case Account.ALGO_SHA512:
                        account.setAlgorithm(Account.ALGO_SHA512);
                        break;
                    default:
                        // invalid algorithm type
                        Timber.d("Invalid algorithm selected: %s", rawAlgo);
                        hasError = true;
                }
            } else {
                Timber.d("Algorithm is empty");
                hasError = true;
            }

            return !hasError;
        }

        @Nullable
        private String trim(@Nullable String data) {
            if (data == null) {
                return null;
            } else {
                return data.trim();
            }
        }

        private boolean checkAndSetStringData(String value, Function<String, Account> function) {
            try {
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(value.trim())) {
                    function.apply(value.trim());
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
