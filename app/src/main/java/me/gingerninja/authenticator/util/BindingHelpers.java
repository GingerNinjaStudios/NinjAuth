package me.gingerninja.authenticator.util;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

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

    @BindingAdapter("currentProgress")
    public static void setCurrentProgress(@NonNull CircularProgressIndicator progressIndicator, double value) {
        progressIndicator.setCurrentProgress(value);
    }

    @BindingAdapter("maxProgress")
    public static void setMaxProgress(@NonNull CircularProgressIndicator progressIndicator, double value) {
        progressIndicator.setMaxProgress(value);
    }

    @BindingAdapter("textAdapter")
    public static void setProgressTextAdapter(@NonNull CircularProgressIndicator progressIndicator, CircularProgressIndicator.ProgressTextAdapter adapter) {
        progressIndicator.setProgressTextAdapter(adapter);
    }

}
