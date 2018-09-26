package me.gingerninja.authenticator.util;

import com.google.android.material.textfield.TextInputLayout;

import androidx.databinding.BindingAdapter;

public class BindingHelpers {
    @BindingAdapter("errorText")
    public static void setErrorText(TextInputLayout view, int errorTextRes) {
        if (errorTextRes == 0) {
            view.setErrorEnabled(false);
            view.setError(null);
        } else {
            view.setErrorEnabled(true);
            view.setError(view.getContext().getString(errorTextRes));
        }
    }

}
