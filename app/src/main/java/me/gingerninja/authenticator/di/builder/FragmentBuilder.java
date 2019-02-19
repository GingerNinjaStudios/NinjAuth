package me.gingerninja.authenticator.di.builder;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.gingerninja.authenticator.di.module.fragment.AccountListModule;
import me.gingerninja.authenticator.di.module.fragment.LabelListModule;
import me.gingerninja.authenticator.ui.account.camera.AddAccountFromCameraFragment;
import me.gingerninja.authenticator.ui.home.AccountListFragment;
import me.gingerninja.authenticator.ui.home.DeleteAccountBottomFragment;
import me.gingerninja.authenticator.ui.home.form.AccountEditorFragment;
import me.gingerninja.authenticator.ui.home.form.LabelSelectorDialogFragment;
import me.gingerninja.authenticator.ui.label.LabelsBottomFragment;
import me.gingerninja.authenticator.ui.label.form.LabelEditorFragment;
import me.gingerninja.authenticator.ui.settings.SettingsFragment;
import me.gingerninja.authenticator.ui.settings.SettingsScreenFragment;
import me.gingerninja.authenticator.ui.setup.SplashFragment;

@Module
public abstract class FragmentBuilder {
    /*@ContributesAndroidInjector
    abstract BaseFragment bindBaseFragment();*/

    @ContributesAndroidInjector
    abstract SplashFragment bindSplashFragment();

    @ContributesAndroidInjector(modules = AccountListModule.class)
    abstract AccountListFragment bindAccountListFragment();

    @ContributesAndroidInjector
    abstract AddAccountFromCameraFragment bindAddAccountFromCameraFragment();

    @ContributesAndroidInjector
    abstract AccountEditorFragment bindAccountEditorFragment();

    @ContributesAndroidInjector
    abstract DeleteAccountBottomFragment bindDeleteAccountBottomFragment();

    @ContributesAndroidInjector
    abstract SettingsFragment bindSettingsFragment();

    @ContributesAndroidInjector
    abstract SettingsScreenFragment bindSettingsScreenFragment();

    @ContributesAndroidInjector(modules = LabelListModule.class)
    abstract LabelsBottomFragment bindLabelsBottomFragment();

    @ContributesAndroidInjector
    abstract LabelEditorFragment bindLabelEditorFragment();

    @ContributesAndroidInjector
    abstract LabelSelectorDialogFragment bindLabelSelectorDialogFragment();
}
