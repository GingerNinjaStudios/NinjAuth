package me.gingerninja.authenticator.ui.setup;

import android.content.Context;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;
import me.gingerninja.authenticator.R;

public class SetupViewModel extends ViewModel {
    public ObservableField<String> theme = new ObservableField<>();

    @Inject
    public SetupViewModel(@NonNull Context context) {
        theme.set(context.getString(R.string.settings_appearance_theme_dark_value));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
