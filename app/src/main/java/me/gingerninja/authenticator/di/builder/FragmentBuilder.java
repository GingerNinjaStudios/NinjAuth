package me.gingerninja.authenticator.di.builder;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.gingerninja.authenticator.di.module.fragment.AccountListModule;
import me.gingerninja.authenticator.ui.account.camera.AddAccountFromCameraFragment;
import me.gingerninja.authenticator.ui.account.form.AddAccountFragment;
import me.gingerninja.authenticator.ui.account.form.EditAccountFragment;
import me.gingerninja.authenticator.ui.home.AccountListFragment;
import me.gingerninja.authenticator.ui.settings.SettingsFragment;
import me.gingerninja.authenticator.ui.settings.SettingsScreenFragment;

@Module
public abstract class FragmentBuilder {
    /*@ContributesAndroidInjector
    abstract BaseFragment bindBaseFragment();*/

    @ContributesAndroidInjector(modules = AccountListModule.class)
    abstract AccountListFragment bindAccountListFragment();

    @ContributesAndroidInjector
    abstract AddAccountFromCameraFragment bindAddAccountFromCameraFragment();

    @ContributesAndroidInjector
    abstract AddAccountFragment bindAddAccountFragment();

    @ContributesAndroidInjector
    abstract EditAccountFragment bindEditAccountFragment();

    @ContributesAndroidInjector
    abstract SettingsFragment bindSettingsFragment();

    @ContributesAndroidInjector
    abstract SettingsScreenFragment bindSettingsScreenFragment();
}
