package me.gingerninja.authenticator.ui.label;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;

public class LabelListItemViewModel {
    @NonNull
    private final Label label;

    private LabelMenuItemClickListener menuItemClickListener;

    public LabelListItemViewModel(@NonNull Label label) {
        this.label = label;
    }

    public String getName() {
        return label.getName();
    }

    @ColorInt
    public int getColor() {
        return label.getColor();
    }

    public int getAccountCount() {
        return label.getAccounts().size();
    }

    public LabelListItemViewModel setMenuItemClickListener(LabelMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
        return this;
    }

    public void onOverflowClick(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.inflate(R.menu.account_list_item_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (menuItemClickListener != null) {
                menuItemClickListener.onLabelMenuItemClicked(item, label);
                return true;
            }

            return false;
        });
        popupMenu.show();
    }

    public interface LabelMenuItemClickListener {
        void onLabelMenuItemClicked(MenuItem item, Label label);
    }
}
