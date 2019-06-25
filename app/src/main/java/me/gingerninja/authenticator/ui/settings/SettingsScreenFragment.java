package me.gingerninja.authenticator.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.AppSettings;
import me.gingerninja.authenticator.util.RequestCodes;
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
                requireActivity().recreate();
            }
            return true;
        });

        Preference backupPref = findPreference(getString(R.string.settings_backup_create_as_key));
        backupPref.setOnPreferenceClickListener(preference -> {
            getNavController().navigate(R.id.backupFragment);
            //backupUtils.createFile(this.getParentFragment(), SettingsFragment.RC_CREATE_BACKUP, "ninjauth-backup.zip", false);
            return true;
        });

        findPreference(getString(R.string.settings_backup_restore_key)).setOnPreferenceClickListener(preference -> {
            backupUtils.openFile(requireParentFragment(), RequestCodes.RESTORE, false);
            return true;
        });

        findPreference(getString(R.string.settings_protection_key)).setOnPreferenceClickListener(preference -> {
            getNavController().navigate(R.id.action_settingsFragment_to_security_setup);
            return true;
        });
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        if (getParentFragment() instanceof OnPreferenceStartScreenCallback) {
            ((OnPreferenceStartScreenCallback) getParentFragment()).onPreferenceStartScreen(this, preferenceScreen);
        }
    }

    private NavController getNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }
}