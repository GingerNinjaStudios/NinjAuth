package me.gingerninja.authenticator.ui.account;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import me.gingerninja.authenticator.BR;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.AccountHasLabel;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import timber.log.Timber;

public abstract class BaseAccountViewModel extends ViewModel {
    public final ObservableBoolean hasLoaded = new ObservableBoolean(false);
    public Data data;
    public ObservableInt typeSpecificTitle = new ObservableInt();
    public ObservableInt typeSpecificDesc = new ObservableInt();
    private final Observable.OnPropertyChangedCallback typeChangeCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            changeTypeSpecificDetails(data.type.get());
        }
    };
    public ObservableBoolean showAdvanced = new ObservableBoolean(false);
    protected Account account;
    @NonNull
    protected AccountRepository accountRepository;

    protected SingleSubject<List<LabelData>> labels = SingleSubject.create();
    protected CompositeDisposable disposable = new CompositeDisposable();

    public BaseAccountViewModel(@NonNull AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        if (data != null && data.type != null) {
            data.type.removeOnPropertyChangedCallback(typeChangeCallback);
        }
    }

    public void init(@Nullable Bundle bundle) {
        if (account != null) {
            return;
        }

        //if (disposable != null) {
        disposable.clear();
        //}

        if (data == null) {
            data = createData();
        }

        disposable.add(getAccount(bundle)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountEntity -> {
                    account = accountEntity;
                    initFieldsFromAccount();
                    changeTypeSpecificDetails(data.type.get());
                    data.type.addOnPropertyChangedCallback(typeChangeCallback);
                    hasLoaded.set(true);
                }, throwable -> {
                    // TODO
                    Timber.e(throwable, "Error loading account: %s", throwable.getMessage());
                })
        );
    }

    protected abstract long getIdFromBundle(@Nullable Bundle bundle);

    private Single<Account> getAccount(@Nullable Bundle bundle) {
        long id = getIdFromBundle(bundle);

        Timber.v("Getting account with ID #%d", id);

        if (id == 0) {
            return Single.just(createAccount(bundle));
        } else {
            return accountRepository.getAccount(id);
        }
    }

    @NonNull
    public SingleSubject<List<LabelData>> getLabels() {
        return labels;
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
    protected abstract Account createAccount(@Nullable Bundle bundle);

    protected void initLabels() {
        if (account.getId() == 0) {
            labels.onSuccess(new ArrayList<>());
            return;
        }

        accountRepository
                .getLabelsByAccount(account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(LabelData::new)
                .toList()
                .subscribe(labels);
    }

    protected Data createData() {
        return new Data();
    }

    private void initFieldsFromAccount() {
        if (account == null) {
            return;
        }

        data.init(account);

        initLabels();
    }

    public void toggleAdvancedClick(View v) {
        showAdvanced.set(!showAdvanced.get());
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

    public static class LabelData implements Comparable<LabelData> {
        @NonNull
        private final Label label;
        private int position;

        public LabelData(@NonNull Label label, int position) {
            this.label = label;
            this.position = position;
        }

        public LabelData(@NonNull AccountHasLabel accountHasLabel) {
            label = accountHasLabel.getLabel();
            position = accountHasLabel.getPosition();
        }

        @NonNull
        public Label getLabel() {
            return label;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public int compareTo(@NonNull LabelData o) {
            return this.position - o.position;
        }
    }
}
