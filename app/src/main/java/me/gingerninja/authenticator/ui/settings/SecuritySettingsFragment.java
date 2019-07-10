package me.gingerninja.authenticator.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

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
        Preference bioPreference = requirePreference(R.string.settings_security_bio_key);

        if (!features.isBiometricsSupported()) {
            bioPreference.setEnabled(false);
            bioPreference.setSummary(R.string.settings_security_bio_unsupported);
        } else {
            bioPreference.setSummaryProvider(new BiometricsSummary());
        }

        requirePreference(R.string.settings_security_lock_key)
                .setSummaryProvider(new LockTypeSummary());


        requirePreference(R.string.settings_security_hide_recent_key)
                .setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean hide = (boolean) newValue;
                    Window window = requireActivity().getWindow();
                    if (hide) {
                        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }
                    return true;
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        String lockType = getPreferenceManager().getSharedPreferences().getString(getString(R.string.settings_security_lock_key), getString(R.string.settings_prot_none_value));
        requirePreference(R.string.settings_security_leave_lock_key).setEnabled(!getString(R.string.settings_prot_none_value).equals(lockType));
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

    private class BiometricsSummary implements Preference.SummaryProvider {

        @Override
        public CharSequence provideSummary(Preference preference) {
            SharedPreferences sharedPreferences = preference.getSharedPreferences();
            boolean bioEnabled = sharedPreferences.getBoolean(getString(R.string.settings_security_bio_key), false);

            if (bioEnabled) {
                return getString(R.string.settings_security_bio_summary_on);
            } else {
                return getString(R.string.settings_security_bio_summary_off);
            }
        }
    }
}
