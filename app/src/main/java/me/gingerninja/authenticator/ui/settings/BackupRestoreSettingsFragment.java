package me.gingerninja.authenticator.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.preference.Preference;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.RequestCodes;
import me.gingerninja.authenticator.util.backup.BackupUtils;

public class BackupRestoreSettingsFragment extends BaseSettingsFragment {
    @Inject
    BackupUtils backupUtils;

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();

        if (getString(R.string.settings_backup_create_as_key).equals(key)) {
            getNavController().navigate(R.id.backupFragment);
            return true;
        } else if (getString(R.string.settings_backup_restore_key).equals(key)) {
            backupUtils.openFile(this, RequestCodes.RESTORE, false);
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCodes.RESTORE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri uri = backupUtils.getUriFromIntent(data);
                    if (uri != null) {
                        BackupRestoreSettingsFragmentDirections.SettingsToRestoreFragmentAction restoreAction = BackupRestoreSettingsFragmentDirections.settingsToRestoreFragmentAction(uri);
                        navigateForResult(RequestCodes.RESTORE).navigate(restoreAction);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected String getTitle() {
        return "Backup & Restore"; // TODO
    }

    @Override
    protected int getSettingsXmlId() {
        return R.xml.settings_backup_restore;
    }
}
