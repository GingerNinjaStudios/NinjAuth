package me.gingerninja.authenticator.ui.label;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;

public class LabelListItemViewModel {
    @NonNull
    private final Label label;

    private LabelMenuItemClickListener menuItemClickListener;

    private int fgColor;
    private int colorControlNormal;

    public LabelListItemViewModel(@NonNull Label label, @NonNull View view) {
        this.label = label;
        boolean isDark = ColorUtils.calculateLuminance(label.getColor()) < 0.5;
        Resources resources = view.getResources();
        fgColor = isDark ? resources.getColor(R.color.colorLabelTextLight) : resources.getColor(R.color.colorLabelTextDark);

        final TypedValue tv = new TypedValue();
        final Context ctx;
        if (isDark) {
            // light color needed
            ctx = new ContextThemeWrapper(view.getContext(), R.style.AppTheme_Dark);
        } else {
            // dark color needed
            ctx = new ContextThemeWrapper(view.getContext(), R.style.AppTheme_Light);
        }
        ctx.getTheme().resolveAttribute(R.attr.colorControlNormal, tv, true);

        colorControlNormal = ContextCompat.getColor(ctx, tv.resourceId);
    }

    public String getName() {
        return label.getName();
    }

    @ColorInt
    public int getColor() {
        return label.getColor();
    }

    @ColorInt
    public int getForegroundColor() {
        return fgColor;
    }

    @ColorInt
    public int getColorControlNormal() {
        return colorControlNormal;
    }

    @DrawableRes
    public int getIcon() {
        return label.getIconResourceId();
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
