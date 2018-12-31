package me.gingerninja.authenticator.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;
import me.gingerninja.authenticator.R;

@InverseBindingMethods({
        @InverseBindingMethod(
                type = MaterialSpinner.class,
                attribute = "value",
                event = "valueAttrChanged",
                method = "getValue")
})
@BindingMethods({

        @BindingMethod(
                type = MaterialSpinner.class,
                attribute = "valueAttrChanged",
                method = "setValueChangeListener"
        )
})
public class MaterialSpinner extends TextInputEditText implements PopupMenu.OnMenuItemClickListener {
    private CharSequence[] displayEntries;
    private CharSequence[] menuEntries;
    private CharSequence[] values;
    private CharSequence value;

    private InverseBindingListener valueChangeListener;

    public MaterialSpinner(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MaterialSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MaterialSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        Drawable dropdownDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_arrow_drop_down_24dp);
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, dropdownDrawable, null);
        setInputType(InputType.TYPE_NULL);
        setSingleLine();

        if (attrs != null) {
            Resources.Theme theme = context.getTheme();
            TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner, defStyleAttr, 0);

            int n = a.getIndexCount();

            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);

                switch (attr) {
                    case R.styleable.MaterialSpinner_menuEntries:
                        setMenuEntries(a.getTextArray(attr));
                        break;
                    case R.styleable.MaterialSpinner_displayEntries:
                        setDisplayEntries(a.getTextArray(attr));
                        break;
                    case R.styleable.MaterialSpinner_values:
                        setValues(a.getTextArray(attr));
                        break;
                    case R.styleable.MaterialSpinner_value:
                        setValue(a.getString(attr));
                        break;
                }
            }

            a.recycle();
        }

        setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            Menu menu = popup.getMenu();

            for (int i = 0; i < menuEntries.length; i++) {
                menu.add(0, i, 0, menuEntries[i]);
            }
            popup.setOnMenuItemClickListener(MaterialSpinner.this);
            popup.show();
        });

        setClickable(true);
        setFocusableInTouchMode(false);
    }

    public CharSequence[] getMenuEntries() {
        return menuEntries;
    }

    public void setMenuEntries(CharSequence[] menuEntries) {
        this.menuEntries = menuEntries;
        refreshText();
    }

    public CharSequence[] getDisplayEntries() {
        return displayEntries;
    }

    public void setDisplayEntries(CharSequence[] displayEntries) {
        this.displayEntries = displayEntries;
        refreshText();
    }

    public CharSequence[] getValues() {
        return values;
    }

    public void setValues(CharSequence[] values) {
        this.values = values;
        refreshText();
    }

    public CharSequence getValue() {
        return value;
    }

    public void setValue(CharSequence value) {
        final boolean changed = !TextUtils.equals(value, this.value);

        this.value = value;
        if (changed) {
            refreshText();
            if (valueChangeListener != null) {
                valueChangeListener.onChange();
            }
        }
    }

    public void setValueChangeListener(InverseBindingListener listener) {
        valueChangeListener = listener;
    }

    private void refreshText() {
        if (values != null && (displayEntries != null || menuEntries != null)) {
            for (int i = 0; i < values.length; i++) {
                if (TextUtils.equals(values[i], value)) {
                    if (displayEntries != null) {
                        setText(displayEntries[i]);
                    } else if (menuEntries != null) {
                        setText(menuEntries[i]);
                    } else {
                        setText(values[i]);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        setValue(values[item.getItemId()]);
        return false;
    }
}
