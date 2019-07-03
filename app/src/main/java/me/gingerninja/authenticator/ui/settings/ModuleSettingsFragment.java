package me.gingerninja.authenticator.ui.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.module.ModuleHandler;

public class ModuleSettingsFragment extends BaseSettingsFragment {
    @Inject
    ModuleHandler moduleHandler;

    @Override
    protected void onPreferencesCreated(@Nullable Bundle savedInstanceState, String rootKey) {
        super.onPreferencesCreated(savedInstanceState, rootKey);

        findPreference(getString(R.string.settings_module_timecorrector_key))
                .setSummaryProvider(new ModuleStateSummaryProvider(ModuleHandler.MODULE_TIME_CORRECTOR));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (getString(R.string.settings_module_timecorrector_key).equals(key)) {
            getNavController().navigate(ModuleSettingsFragmentDirections.openTimeCorrectorSettingsAction());
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_main_modules_title);
    }

    @Override
    protected int getSettingsXmlId() {
        return R.xml.settings_module;
    }

    private class ModuleStateSummaryProvider implements Preference.SummaryProvider {
        @NonNull
        private final String moduleName;

        private ModuleStateSummaryProvider(@NonNull String moduleName) {
            this.moduleName = moduleName;
        }

        @Override
        public CharSequence provideSummary(Preference preference) {
            boolean installed = moduleHandler.isInstalled(moduleName);
            boolean enabled = moduleHandler.isEnabled(moduleName);

            if (!installed) {
                return getString(R.string.settings_module_state_not_installed);
            } else if (enabled) {
                return getString(R.string.settings_module_state_installed_enabled);
            } else {
                return getString(R.string.settings_module_state_installed_disabled);
            }
        }
    }
}
