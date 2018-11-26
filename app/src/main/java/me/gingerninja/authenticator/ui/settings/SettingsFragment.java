package me.gingerninja.authenticator.ui.settings;

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import dagger.android.support.AndroidSupportInjection;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.AppSettings;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Inject
    AppSettings appSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(AppSettings.SHARED_PREFS_NAME);
        setPreferencesFromResource(R.xml.settings, rootKey);

        if (rootKey == null) {
            setupMainPreferences();
        }
    }

    private void setupMainPreferences() {
        Preference themePref = findPreference(getString(R.string.settings_appearance_theme_key));
        themePref.setOnPreferenceChangeListener((preference, newValueObj) -> {
            String oldValue = ((ListPreference) preference).getValue();
            String newValue = (String) newValueObj;
            if (!newValue.equals(oldValue)) {
                getActivity().recreate();
            }
            return true;
        });
    }
}
