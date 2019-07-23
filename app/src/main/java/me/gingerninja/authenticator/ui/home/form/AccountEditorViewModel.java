package me.gingerninja.authenticator.ui.home.form;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.math.BigDecimal;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.ui.account.BaseAccountViewModel;
import me.gingerninja.authenticator.util.CodeGenerator;
import me.gingerninja.authenticator.util.Parser;
import me.gingerninja.authenticator.util.SingleEvent;
import me.gingerninja.authenticator.util.validator.Validator;
import timber.log.Timber;

public class AccountEditorViewModel extends BaseAccountViewModel {
    public static final String NAV_ACTION_SAVE = "account.save";
    public static final String EVENT_EXISTING_ACCOUNT = "event.existing_account";

    public static final int MODE_CREATE = 0;
    public static final int MODE_EDIT = 1;
    public Error error = new Error();
    @Mode
    public int mode;
    private Disposable saveDisposable;

    private MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();
    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    @Nullable
    private Account oldAccount, newAccount;

    @Inject
    public AccountEditorViewModel(@NonNull AccountRepository accountRepository) {
        super(accountRepository);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (saveDisposable != null && !saveDisposable.isDisposed()) {
            saveDisposable.dispose();
        }
    }

    public int getMode() {
        return mode;
    }

    public AccountEditorViewModel setMode(int mode) {
        this.mode = mode;
        return this;
    }

    private boolean checkValues() {
        return ((EditableData) data).prepareAndCheckData(account, error);
    }

    public void onSaveClick(View view) {
        if (checkValues()) {
            saveDisposable = accountRepository
                    .addAccount(account)
                    .ignoreElement()
                    .andThen(accountRepository.saveLabelsForAccount(account, labels.flatMapObservable(Observable::fromIterable).map(LabelData::getLabel).toList().blockingGet()))
                    .subscribe(() -> navAction.postValue(new SingleEvent<>(NAV_ACTION_SAVE, account.getTitle())));

        }
    }

    LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }

    LiveData<SingleEvent> getEvents() {
        return events;
    }

    @Nullable
    Account getExistingAccount() {
        if (mode != MODE_CREATE) {
            throw new IllegalStateException("There's no existing account if the mode is not set to create");
        }
        return oldAccount;
    }

    @Nullable
    Account getNewAccount() {
        if (mode != MODE_CREATE) {
            throw new IllegalStateException("There's no new account if the mode is not set to create");
        }
        return newAccount;
    }

    @SuppressWarnings("unchecked")
    private void checkExistingAccount(Account account) {
        disposable.add(
                accountRepository
                        .findExistingAccount(account)
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                existingAccount -> {
                                    newAccount = account;
                                    oldAccount = existingAccount;
                                    events.postValue(new SingleEvent(EVENT_EXISTING_ACCOUNT, existingAccount));
                                },
                                throwable -> Timber.w(throwable, "Cannot check existing account")
                        )
        );
    }

    @Override
    protected long getIdFromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            return 0;
        } else {
            return AccountEditorFragmentArgs.fromBundle(bundle).getId();
        }
    }

    @NonNull
    @Override
    protected Account createAccount(@Nullable Bundle bundle) {
        Account account;

        if (bundle != null) {
            AccountEditorFragmentArgs args = AccountEditorFragmentArgs.fromBundle(bundle);
            if (args.getUrl() != null) {
                account = Parser.parseUrl(args.getUrl());
                checkExistingAccount(account);
            } else {
                account = new Account();
                showAdvanced.set(true);
            }
        } else {
            account = new Account();
            showAdvanced.set(true);
        }

        // Parser.parseUrl(...) _could_ return null, but not in this case
        // noinspection ConstantConditions
        return account;
    }

    @Override
    protected Data createData() {
        return new EditableData();
    }

    @IntDef({MODE_CREATE, MODE_EDIT})
    @interface Mode {
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
                    createValidator(secret.get(), account::setSecret, error.secret::set)
                            .test(CodeGenerator::isValidSecret, R.string.error_invalid_secret),
                    Validator.from(issuer.get(), account::setIssuer, null)
                            .process(input -> input == null ? null : input.trim()),
                    typeValidator,
                    createValidator(algorithm.get(), account::setAlgorithm, null),
                    Validator.from(digits.get(), null, error.digits::set)
                            .notNull(R.string.error_field_empty)
                            .process(String::trim)
                            .test(input -> !input.isEmpty(), R.string.error_field_empty)
                            .test(TextUtils::isDigitsOnly, R.string.error_field_empty)
                            .transform(Integer::parseInt, account::setDigits)
                            .test(value -> value >= 1 && value <= 8, R.string.error_field_number_1to8),
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
