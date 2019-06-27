package me.gingerninja.authenticator.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.crypto.Crypto;

public class SecuritySettingsFragment extends BaseSettingsFragment {
    @Inject
    Crypto crypto;

    @Override
    protected void onPreferencesCreated(@Nullable Bundle savedInstanceState, String rootKey) {
        final Crypto.Features features = crypto.getFeatures();
        if (!features.isBiometricsSupported()) {
            Preference bioPreference = findPreference(getString(R.string.settings_security_bio_key));

            if (bioPreference != null) {
                bioPreference.setEnabled(false);
                bioPreference.setSummary(R.string.settings_security_bio_unsupported);
            }
        }

        Preference lockPreference = findPreference(getString(R.string.settings_security_lock_key));

        if (lockPreference != null) {
            lockPreference.setSummaryProvider(new LockTypeSummary());
        }
    }

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

    private class LockTypeSummary implements Preference.SummaryProvider {

        @Override
        public CharSequence provideSummary(Preference preference) {
            SharedPreferences sharedPreferences = preference.getSharedPreferences();
            String method = sharedPreferences.getString(getString(R.string.settings_security_lock_key), getString(R.string.settings_prot_none_value));

            if (TextUtils.equals(method, getString(R.string.settings_prot_pin_value))) {
                return getString(R.string.settings_prot_pin);
            } else if (TextUtils.equals(method, getString(R.string.settings_prot_password_value))) {
                return getString(R.string.settings_prot_password);
            } else if (TextUtils.equals(method, getString(R.string.settings_prot_bio_pin_value))) { // this should not happen
                return getString(R.string.settings_prot_bio_pin);
            } else if (TextUtils.equals(method, getString(R.string.settings_prot_bio_password_value))) {  // this should not happen
                return getString(R.string.settings_prot_bio_password);
            }

            return getString(R.string.settings_prot_none);
        }
    }
}
