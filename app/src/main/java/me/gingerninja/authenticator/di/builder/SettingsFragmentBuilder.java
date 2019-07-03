package me.gingerninja.authenticator.di.builder;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.gingerninja.authenticator.module.timecorrector.TimeCorrectorSettingsFragment;
import me.gingerninja.authenticator.ui.settings.BackupRestoreSettingsFragment;
import me.gingerninja.authenticator.ui.settings.InfoSettingsFragment;
import me.gingerninja.authenticator.ui.settings.ModuleSettingsFragment;
import me.gingerninja.authenticator.ui.settings.SecuritySettingsFragment;
import me.gingerninja.authenticator.ui.settings.SettingsFragment;

@Module
public abstract class SettingsFragmentBuilder {
    @ContributesAndroidInjector
    abstract SettingsFragment bindSettingsFragment();

    @ContributesAndroidInjector
    abstract SecuritySettingsFragment bindSecuritySettingsFragment();

    @ContributesAndroidInjector
    abstract BackupRestoreSettingsFragment bindBackupRestoreSettingsFragment();

    @ContributesAndroidInjector
    abstract InfoSettingsFragment bindInfoSettingsFragment();

    @ContributesAndroidInjector
    abstract ModuleSettingsFragment bindModuleSettingsFragment();

    @ContributesAndroidInjector
    abstract TimeCorrectorSettingsFragment bindTimeCorrectorSettingsFragment();
}
