package me.gingerninja.authenticator.ui.home.list;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableDouble;
import androidx.databinding.library.baseAdapters.BR;

import java.util.concurrent.TimeUnit;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.CodeGenerator;
import timber.log.Timber;

public class AccountListItemHotpViewModel extends AccountListItemViewModel {
    public static final CircularProgressIndicator.ProgressTextAdapter TEXT_ADAPTER = value -> "";
    public static final double MAX_PROGRESS = 1d;

    public ObservableBoolean cooldownInProgress = new ObservableBoolean(false);
    public ObservableDouble cooldownProgress = new ObservableDouble(0);

    @NonNull
    private final AccountRepository accountRepository;

    public AccountListItemHotpViewModel(@NonNull Account account, @NonNull CodeGenerator codeGenerator, @NonNull AccountRepository accountRepository) {
        super(account, codeGenerator);
        this.accountRepository = accountRepository;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void onRefreshButtonClick(View v) {
        cooldownProgress.set(0);
        cooldownInProgress.set(true);

        Observable.intervalRange(0, 50, 0, 20, TimeUnit.MILLISECONDS, Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    cooldownProgress.set(MAX_PROGRESS * ((50 - value) / 50d));
                }, throwable -> {
                    Timber.e(throwable, "Cooldown error");
                }, () -> {
                    cooldownInProgress.set(false);
                });

        accountRepository.getAccount(account.getId())
                .subscribeOn(Schedulers.io())
                .flatMap(acc -> accountRepository.addAccount(acc.setTypeSpecificData(acc.getTypeSpecificData() + 1)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountEntity -> {
                    if (accountEntity != null) {
                        account = accountEntity;
                        notifyPropertyChanged(BR.code);
                    }
                }, throwable -> {
                });
        //account.setTypeSpecificData(account.getTypeSpecificData() + 1L);
        //accountRepository.addAccount(account);
    }
}
