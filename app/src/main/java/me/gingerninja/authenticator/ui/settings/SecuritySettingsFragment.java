package me.gingerninja.authenticator.ui.settings;

import androidx.preference.Preference;

import me.gingerninja.authenticator.R;

public class SecuritySettingsFragment extends BaseSettingsFragment {
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();

        if (getString(R.string.settings_security_lock_key).equals(key)) {
            SecuritySettingsFragmentDirections.OpenSecuritySetupAction action = SecuritySettingsFragmentDirections.openSecuritySetupAction(R.string.settings_security_lock_key);
            getNavController().navigate(action);
            return true;
        } else if (getString(R.string.settings_security_bio_key).equals(key)) {
            SecuritySettingsFragmentDirections.OpenSecuritySetupAction action = SecuritySettingsFragmentDirections.openSecuritySetupAction(R.string.settings_security_bio_key);
            getNavController().navigate(action);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_main_security_title);
    }

    @Override
    protected int getSettingsXmlId() {
        return R.xml.settings_security;
    }
}
