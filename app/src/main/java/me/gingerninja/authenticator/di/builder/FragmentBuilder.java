package me.gingerninja.authenticator.di.builder;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.gingerninja.authenticator.di.module.fragment.AccountFilterModule;
import me.gingerninja.authenticator.di.module.fragment.AccountListModule;
import me.gingerninja.authenticator.di.module.fragment.LabelIconPickerModule;
import me.gingerninja.authenticator.di.module.fragment.LabelListModule;
import me.gingerninja.authenticator.di.module.fragment.RestoreFragmentModule;
import me.gingerninja.authenticator.ui.account.camera.AddAccountFromCameraFragment;
import me.gingerninja.authenticator.ui.account.image.AddAccountFromImageFragment;
import me.gingerninja.authenticator.ui.backup.BackupDialogFragment;
import me.gingerninja.authenticator.ui.backup.BackupFragment;
import me.gingerninja.authenticator.ui.backup.RestoreFragment;
import me.gingerninja.authenticator.ui.backup.RestorePasswordDialogFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreAccountPageFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreLabelPageFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreSummaryPageFragment;
import me.gingerninja.authenticator.ui.home.AccountListFragment;
import me.gingerninja.authenticator.ui.home.DeleteAccountBottomFragment;
import me.gingerninja.authenticator.ui.home.filter.AccountFilterDialogFragment;
import me.gingerninja.authenticator.ui.home.form.AccountEditorFragment;
import me.gingerninja.authenticator.ui.home.form.ExistingAccountDialogFragment;
import me.gingerninja.authenticator.ui.home.form.LabelSelectorDialogFragment;
import me.gingerninja.authenticator.ui.label.DeleteLabelBottomFragment;
import me.gingerninja.authenticator.ui.label.LabelsBottomFragment;
import me.gingerninja.authenticator.ui.label.form.LabelEditorFragment;
import me.gingerninja.authenticator.ui.label.form.LabelIconPickerDialogFragment;
import me.gingerninja.authenticator.ui.security.BiometricsSetFragment;
import me.gingerninja.authenticator.ui.security.LockTypeSelectorFragment;
import me.gingerninja.authenticator.ui.security.PasswordCheckFragment;
import me.gingerninja.authenticator.ui.security.PasswordSetFragment;
import me.gingerninja.authenticator.ui.security.StartupPasswordCheckFragment;
import me.gingerninja.authenticator.ui.setup.SetupCompleteFragment;
import me.gingerninja.authenticator.ui.setup.SplashFragment;
import me.gingerninja.authenticator.ui.setup.security.SecuritySetupFragment;
import me.gingerninja.authenticator.ui.setup.theme.ThemeSetupFragment;

@Module(includes = {SettingsFragmentBuilder.class})
public abstract class FragmentBuilder {
    /*@ContributesAndroidInjector
    abstract BaseFragment bindBaseFragment();*/

    @ContributesAndroidInjector
    abstract SplashFragment bindSplashFragment();

    @ContributesAndroidInjector
    abstract StartupPasswordCheckFragment bindStartupPasswordCheckFragment();

    @ContributesAndroidInjector
    abstract ThemeSetupFragment bindThemeSetupFragment();

    @ContributesAndroidInjector
    abstract SecuritySetupFragment bindSecuritySetupFragment();

    @ContributesAndroidInjector
    abstract SetupCompleteFragment bindSetupCompleteFragment();

    @ContributesAndroidInjector(modules = AccountListModule.class)
    abstract AccountListFragment bindAccountListFragment();

    @ContributesAndroidInjector(modules = AccountFilterModule.class)
    abstract AccountFilterDialogFragment bindAccountFilterDialogFragment();

    @ContributesAndroidInjector
    abstract AddAccountFromCameraFragment bindAddAccountFromCameraFragment();

    @ContributesAndroidInjector
    abstract AddAccountFromImageFragment bindAddAccountFromImageFragment();

    @ContributesAndroidInjector
    abstract AccountEditorFragment bindAccountEditorFragment();

    @ContributesAndroidInjector
    abstract ExistingAccountDialogFragment bindExistingAccountDialogFragment();

    @ContributesAndroidInjector
    abstract DeleteAccountBottomFragment bindDeleteAccountBottomFragment();

    @ContributesAndroidInjector
    abstract BackupFragment bindBackupFragment();

    @ContributesAndroidInjector
    abstract BackupDialogFragment bindBackupDialogFragment();

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
    abstract DeleteLabelBottomFragment bindDeleteLabelBottomFragment();

    @ContributesAndroidInjector
    abstract LabelEditorFragment bindLabelEditorFragment();

    @ContributesAndroidInjector(modules = LabelIconPickerModule.class)
    abstract LabelIconPickerDialogFragment bindLabelIconSelectorDialogFragment();

    @ContributesAndroidInjector
    abstract LabelSelectorDialogFragment bindLabelSelectorDialogFragment();

    @ContributesAndroidInjector
    abstract PasswordCheckFragment bindPasswordCheckFragment();

    @ContributesAndroidInjector
    abstract LockTypeSelectorFragment bindLockTypeSelectorFragment();

    @ContributesAndroidInjector
    abstract PasswordSetFragment bindPasswordSetFragment();

    @ContributesAndroidInjector
    abstract BiometricsSetFragment bindBiometricsSetFragment();
}
