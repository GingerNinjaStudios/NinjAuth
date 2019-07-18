package me.gingerninja.authenticator.ui.home.list;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.util.CodeGenerator;

public class AccountListItemViewModel extends BaseObservable {
    public static final int MODE_IDLE = 0;
    public static final int MODE_DRAG = 1;

    @NonNull
    protected final CodeGenerator codeGenerator;
    @NonNull
    protected Account account;
    protected AccountMenuItemClickListener menuItemClickListener;
    @Mode
    protected int mode = MODE_IDLE;

    public AccountListItemViewModel(@NonNull Account account, @NonNull CodeGenerator codeGenerator) {
        this.account = account;
        this.codeGenerator = codeGenerator;
    }

    public void setMenuItemClickListener(AccountMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
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

    @NonNull
    public Account getAccount() {
        return account;
    }

    public String getAccountName() {
        return account.getTitle();
    }

    @Bindable
    public String getCode() {
        return codeGenerator.getFormattedCode(account);
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

    @IntDef({MODE_IDLE, MODE_DRAG})
    @interface Mode {
    }

    public interface AccountMenuItemClickListener {
        void onAccountMenuItemClicked(MenuItem item, Account account);
    }
}
