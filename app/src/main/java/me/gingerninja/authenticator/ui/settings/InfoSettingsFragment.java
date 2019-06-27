package me.gingerninja.authenticator.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import de.psdev.licensesdialog.LicensesDialogFragment;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import me.gingerninja.authenticator.BuildConfig;
import me.gingerninja.authenticator.R;

public class InfoSettingsFragment extends BaseSettingsFragment {
    @Override
    protected void onPreferencesCreated(@Nullable Bundle savedInstanceState, String rootKey) {
        findPreference("settings_info_version").setSummaryProvider(new VersionInfoProvider());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if ("settings_info_app_license".equals(preference.getKey())) {
            Notice appNotice = new Notice();
            appNotice.setName(getString(R.string.app_name));
            appNotice.setCopyright("Copyright 2019 Gergely Kőrössy");
            appNotice.setLicense(new MITLicense());
            appNotice.setUrl("https://www.ninjauth.app");

            new LicensesDialogFragment.Builder(requireContext())
                    .setNotice(appNotice)
                    .setIncludeOwnLicense(false)
                    .build()
                    .show(getChildFragmentManager(), "settings_info_app_license");
            return true;
        } else if ("settings_info_os_licenses".equals(preference.getKey())) {
            new LicensesDialogFragment.Builder(requireContext())
                    .setNotices(R.raw.licenses)
                    .build()
                    .show(getChildFragmentManager(), "settings_info_os_licenses");
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_main_info_title);
    }

    @Override
    protected int getSettingsXmlId() {
        return R.xml.settings_info;
    }

    static class VersionInfoProvider implements Preference.SummaryProvider {

        @Override
        public CharSequence provideSummary(Preference preference) {
            return BuildConfig.VERSION_NAME;
        }
    }
}
