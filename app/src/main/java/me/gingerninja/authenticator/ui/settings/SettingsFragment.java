package me.gingerninja.authenticator.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.AppSettings;
import me.gingerninja.authenticator.util.backup.BackupUtils;

public class SettingsFragment extends BaseSettingsFragment {
    @Inject
    AppSettings appSettings;

    @Inject
    BackupUtils backupUtils;

    @Override
    protected void onPreferencesCreated(@Nullable Bundle savedInstanceState, String rootKey) {
        Preference themePref = findPreference(getString(R.string.settings_main_theme_key));

        if (themePref != null) {
            themePref.setOnPreferenceChangeListener((preference, newValueObj) -> {
                appSettings.setTemporaryTheme(null);

                String oldValue = ((ListPreference) preference).getValue();
                String newValue = (String) newValueObj;
                if (!newValue.equals(oldValue)) {
                    requireActivity().recreate();
                }
                return true;
            });
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        if (getString(R.string.settings_main_backup_key).equals(key)) {
            getNavController().navigate(SettingsFragmentDirections.openBackupRestoreSettingsAction());
            return true;
        } else if (getString(R.string.settings_main_security_key).equals(key)) {
            getNavController().navigate(SettingsFragmentDirections.openSecuritySettingsAction());
            return true;
        } else if (getString(R.string.settings_main_modules_key).equals(key)) {
            getNavController().navigate(SettingsFragmentDirections.openModuleSettingsAction());
            return true;
        } else if (getString(R.string.settings_main_info_key).equals(key)) {
            getNavController().navigate(SettingsFragmentDirections.openInfoSettingsAction());
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.nav_settings_title);
    }

    @Override
    protected int getSettingsXmlId() {
        return R.xml.settings;
    }
}
