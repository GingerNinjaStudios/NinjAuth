package me.gingerninja.authenticator.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.databinding.BindingAdapter;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import me.gingerninja.authenticator.R;

public class BindingHelpers {
    @BindingAdapter("text")
    public static void setText(TextView textView, int textRes) {
        textView.setText(textRes == 0 ? null : textView.getContext().getString(textRes));
    }

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

    @BindingAdapter("hint")
    public static void setHintText(TextInputLayout view, int hintTextRes) {
        if (hintTextRes == 0) {
            view.setHintEnabled(false);
            view.setHint(null);
        } else {
            view.setHintEnabled(true);
            view.setHint(view.getContext().getString(hintTextRes));
        }
    }

    @BindingAdapter("helperText")
    public static void setHelperText(TextInputLayout view, int helperTextRes) {
        if (helperTextRes == 0) {
            view.setHelperTextEnabled(false);
            view.setHelperText(null);
        } else {
            view.setHelperTextEnabled(true);
            view.setHelperText(view.getContext().getString(helperTextRes));
        }
    }

    private static int grayscale(int color) {
        int uniformValue = (int) (Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114);
        return Color.rgb(uniformValue, uniformValue, uniformValue);
    }

    @BindingAdapter(value = {"progressColor", "dotColor", "enabled"}, requireAll = false)
    public static void setProgressAndDotColor(@NonNull CircularProgressIndicator progressIndicator, @AttrRes int progressResId, @AttrRes int dotResId, boolean enabled) {
        progressIndicator.setEnabled(enabled);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = progressIndicator.getContext().getTheme();

        if (progressResId != 0) {
            theme.resolveAttribute(progressResId, typedValue, true);
            if (enabled) {
                progressIndicator.setProgressColor(typedValue.data);
            } else {
                progressIndicator.setProgressColor(grayscale(typedValue.data));
            }
        }

        if (dotResId != 0) {
            theme.resolveAttribute(dotResId, typedValue, true);
            if (enabled) {
                progressIndicator.setDotColor(typedValue.data);
            } else {
                progressIndicator.setDotColor(grayscale(typedValue.data));
            }
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

    @BindingAdapter("readableHintTextColorFromBackground")
    public static void setHintTextColor(@NonNull TextInputLayout view, int bgColor) {
        int fgColor = ColorUtils.calculateLuminance(bgColor) < 0.5 ? view.getResources().getColor(R.color.colorLabelTextLight) : view.getResources().getColor(R.color.colorLabelTextDark);
        view.setDefaultHintTextColor(ColorStateList.valueOf(fgColor));
    }

    @BindingAdapter("readableChipTextColorFromBackground")
    public static void setChipTextColor(@NonNull Chip view, int bgColor) {
        int fgColor = ColorUtils.calculateLuminance(bgColor) < 0.5 ? view.getResources().getColor(R.color.colorLabelTextLight) : view.getResources().getColor(R.color.colorLabelTextDark);
        ColorStateList fgColorStateList = ColorStateList.valueOf(fgColor);

        int[][] closeIconStates = {
                {android.R.attr.state_pressed},
                {android.R.attr.state_focused, android.R.attr.state_hovered},
                {android.R.attr.state_focused},
                {android.R.attr.state_hovered},
                {android.R.attr.state_enabled},
                {}
        };
        int[] closeIconColors = {
                ColorUtils.setAlphaComponent(fgColor, 0xff),
                ColorUtils.setAlphaComponent(fgColor, 0xff),
                ColorUtils.setAlphaComponent(fgColor, 0xde),
                ColorUtils.setAlphaComponent(fgColor, 0xb8),
                ColorUtils.setAlphaComponent(fgColor, 0x8a),
                ColorUtils.setAlphaComponent(fgColor, 0x36)
        };
        ColorStateList closeIconTint = new ColorStateList(closeIconStates, closeIconColors);

        int[][] rippleStates = {
                {android.R.attr.state_pressed},
                {android.R.attr.state_focused, android.R.attr.state_hovered},
                {android.R.attr.state_focused},
                {android.R.attr.state_hovered},
                {}
        };
        int[] rippleColors = {
                ColorUtils.setAlphaComponent(fgColor, 0x29),
                ColorUtils.setAlphaComponent(fgColor, 0x29),
                ColorUtils.setAlphaComponent(fgColor, 0x1f),
                ColorUtils.setAlphaComponent(fgColor, 0x0a),
                0
        };
        ColorStateList rippleColorStateList = new ColorStateList(rippleStates, rippleColors);

        view.setTextColor(fgColorStateList);
        view.setCloseIconTint(closeIconTint);
        view.setChipIconTint(fgColorStateList);
        view.setRippleColor(rippleColorStateList);
    }

    @BindingAdapter("srcCompatRes")
    public static void setCompatImageViewDrawable(ImageView imageView, @DrawableRes int resId) {
        imageView.setImageResource(resId);
    }

    @BindingAdapter("srcCompat")
    public static void setCompatImageViewDrawable(final FloatingActionButton fab, final Drawable resId) {
        final Drawable oldDrawable = fab.getDrawable();
        if (oldDrawable == null) {
            fab.setImageDrawable(resId);
        } else {
            fab.addOnShowAnimationListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);

                    fab.setImageDrawable(resId);
                    fab.removeOnShowAnimationListener(this);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    fab.setImageDrawable(resId);
                    fab.removeOnShowAnimationListener(this);
                }
            });
        }
    }

    @BindingAdapter("menu")
    public static void setCompatImageViewDrawable(BottomAppBar bottomAppBar, @MenuRes int resId) {
        if (resId == 0) {
            bottomAppBar.getMenu().clear();
        } else {
            bottomAppBar.replaceMenu(resId);
        }
    }

    @BindingAdapter("onEditorActionListener")
    public static void setOnEditorActionListener(TextView view, TextView.OnEditorActionListener listener) {
        view.setOnEditorActionListener(listener);
    }

    @BindingAdapter("selectAll")
    public static void setOnEditorActionListener(EditText view, boolean selectAll) {
        if (selectAll) {
            view.selectAll();

            view.clearFocus();
            view.requestFocus();

            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @BindingAdapter("passwordToggleEnabled")
    public static void setPasswordToggleEnabled(TextInputLayout view, boolean enabled) {
        view.setEndIconMode(enabled ? TextInputLayout.END_ICON_PASSWORD_TOGGLE : TextInputLayout.END_ICON_NONE);
    }
}
