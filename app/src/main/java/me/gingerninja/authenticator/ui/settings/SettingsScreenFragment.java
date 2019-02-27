package me.gingerninja.authenticator.ui.settings;

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import dagger.android.support.AndroidSupportInjection;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.AppSettings;
import me.gingerninja.authenticator.util.backup.BackupUtils;

public class SettingsScreenFragment extends PreferenceFragmentCompat {

    @Inject
    AppSettings appSettings;

    @Inject
    BackupUtils backupUtils;

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
            appSettings.setTemporaryTheme(null);

            String oldValue = ((ListPreference) preference).getValue();
            String newValue = (String) newValueObj;
            if (!newValue.equals(oldValue)) {
                getActivity().recreate();
            }
            return true;
        });

        Preference backupPref = findPreference(getString(R.string.settings_backup_create_as_key));
        backupPref.setOnPreferenceClickListener(preference -> {
            backupUtils.createFile(this.getParentFragment(), SettingsFragment.RC_CREATE_BACKUP, "ninjauth-backup.zip", false);
            return true;
        });

        findPreference(getString(R.string.settings_backup_restore_key)).setOnPreferenceClickListener(preference -> {
            backupUtils.openFile(this.getParentFragment(), SettingsFragment.RC_RESTORE, false);
            return true;
        });
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        if (getParentFragment() instanceof OnPreferenceStartScreenCallback) {
            ((OnPreferenceStartScreenCallback) getParentFragment()).onPreferenceStartScreen(this, preferenceScreen);
        }
    }
}