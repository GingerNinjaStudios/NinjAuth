package me.gingerninja.authenticator.module.timecorrector;

import androidx.preference.Preference;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.module.ModuleHandler;
import me.gingerninja.authenticator.ui.settings.BaseSettingsFragment;
import timber.log.Timber;

public class TimeCorrectorSettingsFragment extends BaseSettingsFragment {
    @Inject
    ModuleHandler moduleHandler;

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        if (isPreferenceKey(key, R.string.settings_module_timecorrector_install_key)) {
            moduleHandler.uninstall(ModuleHandler.MODULE_TIME_CORRECTOR).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
                Timber.v("Module '%s' uninstalled", ModuleHandler.MODULE_TIME_CORRECTOR);
            }, throwable -> {
                Timber.e(throwable, "Module '%s' uninstall failed", ModuleHandler.MODULE_TIME_CORRECTOR);
            });
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_timecorrector);
    }

    @Override
    protected int getSettingsXmlId() {
        return R.xml.settings_module_timecorrector;
    }

    @Override
    protected String getSharedPreferencesName() {
        return ModuleHandler.SHARED_PREF_NAME;
    }
}
