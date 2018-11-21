package me.gingerninja.authenticator.ui.home.list;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.BR;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.util.CodeGenerator;

public class AccountListItemViewModel extends BaseObservable {
    public static final CircularProgressIndicator.ProgressTextAdapter TEXT_ADAPTER = new CircularProgressIndicator.ProgressTextAdapter() {
        @NonNull
        @Override
        public String formatText(double value) {
            return "";
        }
    };

    @NonNull
    private final Account account;

    @NonNull
    private final CodeGenerator codeGenerator;

    private Disposable clockDisposable;

    public AccountListItemViewModel(@NonNull Account account, @NonNull CodeGenerator codeGenerator) {
        this.account = account;
        this.codeGenerator = codeGenerator;
    }

    public void startClock(Observable<Long> clock) {
        stopClock();

        clockDisposable = clock.subscribe(this::onTimeUpdate);
    }

    public void stopClock() {
        if (clockDisposable != null && !clockDisposable.isDisposed()) {
            clockDisposable.dispose();
        }
    }

    private void onTimeUpdate(Long seq) {
        notifyPropertyChanged(BR.code);
        notifyPropertyChanged(BR.secondsLeft);
        notifyPropertyChanged(BR.currentProgress);
    }

    public String getAccountName() {
        return account.getAccountName();
    }

    @Bindable
    public String getCode() {
        return codeGenerator.getFormattedCode(account);
    }

    @Bindable
    public long getPeriod() {
        return account.getTypeSpecificData();
    }

    @Bindable
    public long getSecondsLeft() {
        return codeGenerator.getRemainingTime(account, TimeUnit.SECONDS);
    }

    @Bindable
    public double getMaxProgress() {
        return 1000d;
    }

    @Bindable
    public double getCurrentProgress() {
        long period = TimeUnit.MILLISECONDS.convert(Math.max(1, account.getTypeSpecificData()), TimeUnit.SECONDS);
        return codeGenerator.getRemainingTime(account, TimeUnit.MILLISECONDS) * 1000d / period;
    }
}
