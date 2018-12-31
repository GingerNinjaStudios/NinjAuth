package me.gingerninja.authenticator.ui.account;

import android.text.TextUtils;
import android.view.View;

import java.math.BigDecimal;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public abstract class BaseEditableAccountViewModel extends BaseAccountViewModel {
    public static final int MODE_CREATE = 0;
    public static final int MODE_EDIT = 1;

    @IntDef({MODE_CREATE, MODE_EDIT})
    @interface Mode {
    }

    public static final String NAV_ACTION_SAVE = "account.save";

    public Error error = new Error();

    @Mode
    public final int mode;

    @NonNull
    protected AccountRepository accountRepository;

    protected MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();

    protected Disposable saveDisposable;

    public BaseEditableAccountViewModel(@NonNull AccountRepository accountRepository, @Mode int mode) {
        this.accountRepository = accountRepository;
        this.mode = mode;
    }

    protected boolean checkValues() {
        return ((EditableData) data).prepareAndCheckData(account, error);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (saveDisposable != null && !saveDisposable.isDisposed()) {
            saveDisposable.dispose();
        }
    }

    public void onSaveClick(View view) {
        if (checkValues()) {
            saveDisposable = accountRepository
                    .addAccount(account)
                    .subscribe(account -> navAction.postValue(new SingleEvent<>(NAV_ACTION_SAVE, account.getTitle())));

        }
    }

    public int getMode() {
        return mode;
    }

    public LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }

    @Override
    protected Data createData() {
        return new EditableData();
    }

    public static class Error {
        public ObservableInt title = new ObservableInt();
        public ObservableInt accountName = new ObservableInt();
        //public ObservableInt issuer = new ObservableInt();
        public ObservableInt digits = new ObservableInt();
        public ObservableInt typeSpecificData = new ObservableInt(); // period for TOTP and counter for HOTP
        public ObservableInt secret = new ObservableInt();

        //public ObservableInt type = new ObservableInt();
        //public ObservableInt algorithm = new ObservableInt();
    }

    public static class EditableData extends BaseAccountViewModel.Data {

        protected boolean prepareAndCheckData(@NonNull Account account, Error error) {
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

            hasError = !checkAndSetStringData(rawTitle, account::setTitle, error.title::set);
            hasError |= !checkAndSetStringData(rawAccountName, account::setAccountName, error.accountName::set);
            //hasError |= !checkAndSetStringData(rawIssuer, account::setIssuer);
            hasError |= !checkAndSetStringData(rawSecret, account::setSecret, error.secret::set);

            account.setIssuer(rawIssuer);

            Timber.d("Has error so far: %s", hasError);

            if (!TextUtils.isEmpty(rawDigits) && TextUtils.isDigitsOnly(rawDigits)) {
                int realDigits = Integer.parseInt(rawDigits);
                account.setDigits(realDigits);
                if (realDigits >= 1 && realDigits <= 8) {
                    error.digits.set(0);
                } else {
                    error.digits.set(R.string.error_field_number_1to8);
                }
            } else {
                Timber.d("Digits are empty");
                hasError = true;
                error.digits.set(R.string.error_field_empty);
            }

            if (!TextUtils.isEmpty(rawType) && !TextUtils.isEmpty(rawTypeSpecificData) && TextUtils.isDigitsOnly(rawTypeSpecificData)) {
                BigDecimal maxValue = new BigDecimal(Long.MAX_VALUE).add(BigDecimal.ONE).multiply(new BigDecimal(2));
                BigDecimal rawValue = new BigDecimal(rawTypeSpecificData);
                int comparison = rawValue.compareTo(maxValue);
                long parsedData = rawValue.longValue();

                switch (rawType) {
                    case Account.TYPE_TOTP:
                        if (parsedData == 0 || comparison >= 0) {
                            error.typeSpecificData.set(rawValue.compareTo(BigDecimal.ZERO) == 0 ? R.string.error_field_number_zero : R.string.error_field_number_too_large);
                            hasError = true;
                            break;
                        }
                    case Account.TYPE_HOTP:
                        account.setTypeSpecificData(parsedData);
                        break;
                }
            } else {
                Timber.d("Type is empty");
                error.typeSpecificData.set(R.string.error_field_empty);
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

        private boolean checkAndSetStringData(String value, Function<String, Account> function, Consumer<Integer> errorFunction) {
            try {
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(value.trim())) {
                    function.apply(value.trim());
                    errorFunction.accept(0);
                    return true;
                } else {
                    errorFunction.accept(R.string.error_field_empty);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
