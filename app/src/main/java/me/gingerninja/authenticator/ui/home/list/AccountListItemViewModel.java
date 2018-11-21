package me.gingerninja.authenticator.ui.home.list;

import android.view.MenuItem;
import android.view.View;

import java.util.concurrent.TimeUnit;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.BR;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.util.CodeGenerator;

public class AccountListItemViewModel extends BaseObservable {
    public static final int MODE_IDLE = 0;
    public static final int MODE_DRAG = 1;
    public static final int MODE_EDIT = 2;

    @IntDef({MODE_IDLE, MODE_DRAG, MODE_EDIT})
    @interface Mode {
    }

    public static final CircularProgressIndicator.ProgressTextAdapter TEXT_ADAPTER = value -> "";

    @NonNull
    private final Account account;

    @NonNull
    private final CodeGenerator codeGenerator;

    private Disposable clockDisposable;

    private AccountMenuItemClickListener menuItemClickListener;

    @Mode
    private int mode = MODE_IDLE;

    public AccountListItemViewModel(@NonNull Account account, @NonNull CodeGenerator codeGenerator) {
        this.account = account;
        this.codeGenerator = codeGenerator;
    }

    public AccountListItemViewModel setMenuItemClickListener(AccountMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
        return this;
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

    public void onOverflowClick(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.inflate(R.menu.account_list_item_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (menuItemClickListener != null) {
                menuItemClickListener.onAccountMenuItemClicked(item, account);
                return true;
            }

            return false;
        });
        popupMenu.show();
    }

    public String getAccountName() {
        return account.getTitle();
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

    @Mode
    @Bindable
    public int getMode() {
        return mode;
    }

    public void setMode(@Mode int mode) {
        this.mode = mode;
        notifyPropertyChanged(BR.mode);
    }

    public interface AccountMenuItemClickListener {
        void onAccountMenuItemClicked(MenuItem item, Account account);
    }
}
