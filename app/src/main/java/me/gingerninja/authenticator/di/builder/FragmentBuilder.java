package me.gingerninja.authenticator.di.builder;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.gingerninja.authenticator.di.module.fragment.AccountListModule;
import me.gingerninja.authenticator.di.module.fragment.LabelListModule;
import me.gingerninja.authenticator.di.module.fragment.RestoreFragmentModule;
import me.gingerninja.authenticator.di.module.fragment.SetupFragmentModule;
import me.gingerninja.authenticator.ui.account.camera.AddAccountFromCameraFragment;
import me.gingerninja.authenticator.ui.account.image.AddAccountFromImageFragment;
import me.gingerninja.authenticator.ui.backup.BackupFragment;
import me.gingerninja.authenticator.ui.backup.RestoreFragment;
import me.gingerninja.authenticator.ui.backup.RestorePasswordDialogFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreAccountPageFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreLabelPageFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreSummaryPageFragment;
import me.gingerninja.authenticator.ui.home.AccountListFragment;
import me.gingerninja.authenticator.ui.home.DeleteAccountBottomFragment;
import me.gingerninja.authenticator.ui.home.form.AccountEditorFragment;
import me.gingerninja.authenticator.ui.home.form.LabelSelectorDialogFragment;
import me.gingerninja.authenticator.ui.label.LabelsBottomFragment;
import me.gingerninja.authenticator.ui.label.form.LabelEditorFragment;
import me.gingerninja.authenticator.ui.settings.SettingsFragment;
import me.gingerninja.authenticator.ui.settings.SettingsScreenFragment;
import me.gingerninja.authenticator.ui.setup.SetupFragment;
import me.gingerninja.authenticator.ui.setup.SplashFragment;
import me.gingerninja.authenticator.ui.setup.page.BackupSetupPageFragment;
import me.gingerninja.authenticator.ui.setup.page.ProtectionSelectorPageFragment;
import me.gingerninja.authenticator.ui.setup.page.ThemeSelectorPageFragment;

@Module
public abstract class FragmentBuilder {
    /*@ContributesAndroidInjector
    abstract BaseFragment bindBaseFragment();*/

    @ContributesAndroidInjector
    abstract SplashFragment bindSplashFragment();

    @ContributesAndroidInjector(modules = SetupFragmentModule.class)
    abstract SetupFragment bindSetupFragment();

    @ContributesAndroidInjector
    abstract ThemeSelectorPageFragment bindThemeSelectorPageFragment();

    @ContributesAndroidInjector
    abstract ProtectionSelectorPageFragment bindProtectionSelectorPageFragment();

    @ContributesAndroidInjector
    abstract BackupSetupPageFragment bindBackupSetupPageFragment();

    @ContributesAndroidInjector(modules = AccountListModule.class)
    abstract AccountListFragment bindAccountListFragment();

    @ContributesAndroidInjector
    abstract AddAccountFromCameraFragment bindAddAccountFromCameraFragment();

    @ContributesAndroidInjector
    abstract AddAccountFromImageFragment bindAddAccountFromImageFragment();

    @ContributesAndroidInjector
    abstract AccountEditorFragment bindAccountEditorFragment();

    @ContributesAndroidInjector
    abstract DeleteAccountBottomFragment bindDeleteAccountBottomFragment();

    @ContributesAndroidInjector
    abstract SettingsFragment bindSettingsFragment();

    @ContributesAndroidInjector
    abstract SettingsScreenFragment bindSettingsScreenFragment();

    @ContributesAndroidInjector
    abstract BackupFragment bindBackupFragment();

    @ContributesAndroidInjector(modules = RestoreFragmentModule.class)
    abstract RestoreFragment bindRestoreFragment();

    @ContributesAndroidInjector
    abstract RestoreAccountPageFragment bindRestoreAccountPageFragment();

    @ContributesAndroidInjector
    abstract RestoreLabelPageFragment bindRestoreLabelPageFragment();

    @ContributesAndroidInjector
    abstract RestoreSummaryPageFragment bindRestoreSummaryPageFragment();

    @ContributesAndroidInjector
    abstract RestorePasswordDialogFragment bindRestorePasswordDialogFragment();

    @ContributesAndroidInjector(modules = LabelListModule.class)
    abstract LabelsBottomFragment bindLabelsBottomFragment();

    @ContributesAndroidInjector
    abstract LabelEditorFragment bindLabelEditorFragment();

    @ContributesAndroidInjector
    abstract LabelSelectorDialogFragment bindLabelSelectorDialogFragment();
}
