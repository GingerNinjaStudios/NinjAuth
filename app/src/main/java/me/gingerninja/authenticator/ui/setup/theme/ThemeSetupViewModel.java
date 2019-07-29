package me.gingerninja.authenticator.ui.setup.theme;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import me.gingerninja.authenticator.util.AppSettings;
import me.gingerninja.authenticator.widget.MaterialSpinner;

public class ThemeSetupViewModel extends ViewModel {
    public ObservableField<String> theme = new ObservableField<>();

    @NonNull
    private final AppSettings appSettings;

    @Inject
    ThemeSetupViewModel(@NonNull Context context, @NonNull AppSettings appSettings) {
        this.appSettings = appSettings;
        theme.set(appSettings.getTheme());
    }

    public boolean onThemeChanged(MaterialSpinner spinner, @Nullable CharSequence newValue, @Nullable CharSequence oldValue) {
        appSettings.saveTheme(newValue == null ? "dark" : newValue.toString());
        return true;
    }
}
