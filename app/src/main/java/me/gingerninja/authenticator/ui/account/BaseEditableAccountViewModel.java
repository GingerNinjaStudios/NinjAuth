package me.gingerninja.authenticator.ui.account;

import android.text.TextUtils;
import android.view.View;

import java.math.BigDecimal;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.SingleEvent;
import me.gingerninja.authenticator.util.validator.Validator;
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

    protected MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();

    protected Disposable saveDisposable;

    public BaseEditableAccountViewModel(@NonNull AccountRepository accountRepository, @Mode int mode) {
        super(accountRepository);
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
                    .ignoreElement()
                    .andThen(accountRepository.saveLabelsForAccount(account, Observable.fromIterable(labels).map(LabelData::getLabel).toList().blockingGet()))
                    .subscribe(() -> navAction.postValue(new SingleEvent<>(NAV_ACTION_SAVE, account.getTitle())));

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
            Validator<String> typeValidator = createValidator(type.get(), account::setType, null);

            return Validator.all(
                    createValidator(title.get(), account::setTitle, error.title::set),
                    createValidator(accountName.get(), account::setAccountName, error.accountName::set),
                    createValidator(secret.get(), account::setSecret, error.secret::set),
                    Validator.from(issuer.get(), account::setIssuer, null)
                            .process(input -> input == null ? null : input.trim()),
                    typeValidator,
                    createValidator(algorithm.get(), account::setAlgorithm, null),
                    Validator.from(digits.get(), null, error.digits::set)
                            .notNull(R.string.error_field_empty)
                            .process(String::trim)
                            .test(input -> !input.isEmpty(), R.string.error_field_empty)
                            .test(TextUtils::isDigitsOnly, R.string.error_field_empty)
                            .transform(Integer::parseInt, account::setDigits),
                    Validator.from(typeSpecificData.get(), null, error.typeSpecificData::set)
                            .notNull(R.string.error_field_empty)
                            .process(String::trim)
                            .test(input -> !input.isEmpty(), R.string.error_field_empty)
                            .test(TextUtils::isDigitsOnly, R.string.error_field_empty)
                            .test(input -> !typeValidator.hasFailed(), R.string.error_field_empty)
                            .test(input -> {
                                BigDecimal maxValue = new BigDecimal(Long.MAX_VALUE).add(BigDecimal.ONE).multiply(new BigDecimal(2));
                                BigDecimal rawValue = new BigDecimal(input);

                                int comparison = rawValue.compareTo(maxValue);
                                long parsedData = rawValue.longValue();

                                String type1 = typeValidator.getData();
                                if (type1 == null) {
                                    return R.string.error_field_empty;
                                }

                                switch (type1) {
                                    case Account.TYPE_TOTP:
                                        if (parsedData == 0 || comparison >= 0) {
                                            if (rawValue.compareTo(BigDecimal.ZERO) == 0) {
                                                return R.string.error_field_number_zero;
                                            } else {
                                                return R.string.error_field_number_too_large;
                                            }
                                        }
                                    case Account.TYPE_HOTP:
                                        //account.setTypeSpecificData(parsedData);
                                        break;
                                }

                                return Validator.RESULT_OK;
                            })
                            .transform(input -> new BigDecimal(input).longValue(), account::setTypeSpecificData)
            );
        }

        private Validator<String> createValidator(String value, Validator.Consumer<String> onSuccess, Validator.Consumer<Integer> onError) {
            return Validator
                    .from(value, onSuccess, onError)
                    .notNull(R.string.error_field_empty)
                    .process(String::trim)
                    .test(input -> !input.isEmpty(), R.string.error_field_empty);
        }
    }
}
