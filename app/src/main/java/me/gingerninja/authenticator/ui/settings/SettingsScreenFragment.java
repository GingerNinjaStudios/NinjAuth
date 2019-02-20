package me.gingerninja.authenticator.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
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
import timber.log.Timber;

public class SettingsScreenFragment extends PreferenceFragmentCompat {
    public static final int RC_CREATE_BACKUP = 0x2001;
    public static final int RC_RESTORE = 0x2002;

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
            backupUtils.createFile(this, RC_CREATE_BACKUP, "ninjauth-backup.zip", false);
            return true;
        });

        findPreference(getString(R.string.settings_backup_restore_key)).setOnPreferenceClickListener(preference -> {
            backupUtils.openFile(this, RC_RESTORE, false);
            return true;
        });
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        if (getParentFragment() instanceof OnPreferenceStartScreenCallback) {
            ((OnPreferenceStartScreenCallback) getParentFragment()).onPreferenceStartScreen(this, preferenceScreen);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_CREATE_BACKUP:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = backupUtils.getUriFromIntent(data);
                    backupUtils.backup(uri).subscribe(() -> {
                        Snackbar.make(getView(), "Backup created successfully", Snackbar.LENGTH_LONG).show();
                    }, throwable -> {
                        Timber.e(throwable, "Cannot create backup: %s", throwable.getMessage());
                        Snackbar.make(getView(), "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
                }
                break;
            case RC_RESTORE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = backupUtils.getUriFromIntent(data);
                    backupUtils.restore(uri).subscribe(() -> {
                        Snackbar.make(getView(), "Restore successful", Snackbar.LENGTH_LONG).show();
                    }, throwable -> {
                        Timber.e(throwable, "Cannot restore: %s", throwable.getMessage());
                        Snackbar.make(getView(), "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}