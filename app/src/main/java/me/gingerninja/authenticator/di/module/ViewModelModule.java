package me.gingerninja.authenticator.di.module;


import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import me.gingerninja.authenticator.di.ViewModelKey;
import me.gingerninja.authenticator.ui.account.camera.AddAccountFromCameraViewModel;
import me.gingerninja.authenticator.ui.account.image.AddAccountFromImageViewModel;
import me.gingerninja.authenticator.ui.backup.BackupDialogViewModel;
import me.gingerninja.authenticator.ui.backup.BackupViewModel;
import me.gingerninja.authenticator.ui.backup.RestoreContentListViewModel;
import me.gingerninja.authenticator.ui.backup.RestoreViewModel;
import me.gingerninja.authenticator.ui.home.AccountListViewModel;
import me.gingerninja.authenticator.ui.home.DeleteAccountViewModel;
import me.gingerninja.authenticator.ui.home.filter.AccountFilterViewModel;
import me.gingerninja.authenticator.ui.home.form.AccountEditorViewModel;
import me.gingerninja.authenticator.ui.home.form.ExistingAccountViewModel;
import me.gingerninja.authenticator.ui.label.DeleteLabelViewModel;
import me.gingerninja.authenticator.ui.label.LabelsViewModel;
import me.gingerninja.authenticator.ui.label.form.LabelEditorViewModel;
import me.gingerninja.authenticator.ui.label.form.LabelIconPickerViewModel;
import me.gingerninja.authenticator.ui.security.BiometricsSetViewModel;
import me.gingerninja.authenticator.ui.security.LockTypeSelectorViewModel;
import me.gingerninja.authenticator.ui.security.PasswordCheckViewModel;
import me.gingerninja.authenticator.ui.security.PasswordSetViewModel;
import me.gingerninja.authenticator.ui.security.StartupPasswordCheckViewModel;
import me.gingerninja.authenticator.ui.setup.SetupCompleteViewModel;
import me.gingerninja.authenticator.ui.setup.security.SecuritySetupViewModel;
import me.gingerninja.authenticator.ui.setup.theme.ThemeSetupViewModel;
import me.gingerninja.authenticator.viewmodel.NinjaViewModelFactory;

@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(StartupPasswordCheckViewModel.class)
    abstract ViewModel bindStartupPasswordCheckViewModel(StartupPasswordCheckViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ThemeSetupViewModel.class)
    abstract ViewModel bindThemeSetupViewModel(ThemeSetupViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SecuritySetupViewModel.class)
    abstract ViewModel bindSecuritySetupViewModel(SecuritySetupViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SetupCompleteViewModel.class)
    abstract ViewModel bindSetupCompleteViewModel(SetupCompleteViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AccountListViewModel.class)
    abstract ViewModel bindAccountListViewModel(AccountListViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AddAccountFromCameraViewModel.class)
    abstract ViewModel bindAddAccountFromCameraViewModel(AddAccountFromCameraViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AddAccountFromImageViewModel.class)
    abstract ViewModel bindAddAccountFromImageViewModel(AddAccountFromImageViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AccountEditorViewModel.class)
    abstract ViewModel bindAccountEditorViewModel(AccountEditorViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ExistingAccountViewModel.class)
    abstract ViewModel bindExistingAccountViewModel(ExistingAccountViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DeleteAccountViewModel.class)
    abstract ViewModel bindDeleteAccountViewModel(DeleteAccountViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AccountFilterViewModel.class)
    abstract ViewModel bindAccountFilterViewModel(AccountFilterViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LabelsViewModel.class)
    abstract ViewModel bindLabelsViewModel(LabelsViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LabelEditorViewModel.class)
    abstract ViewModel bindLabelEditorViewModel(LabelEditorViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LabelIconPickerViewModel.class)
    abstract ViewModel bindLabelIconSelectorViewModel(LabelIconPickerViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DeleteLabelViewModel.class)
    abstract ViewModel bindDeleteLabelViewModel(DeleteLabelViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(BackupViewModel.class)
    abstract ViewModel bindBackupViewModel(BackupViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(BackupDialogViewModel.class)
    abstract ViewModel bindBackupDialogViewModel(BackupDialogViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(RestoreViewModel.class)
    abstract ViewModel bindRestoreViewModel(RestoreViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(RestoreContentListViewModel.class)
    abstract ViewModel bindRestoreContentListViewModel(RestoreContentListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PasswordCheckViewModel.class)
    abstract ViewModel bindPasswordCheckViewModel(PasswordCheckViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LockTypeSelectorViewModel.class)
    abstract ViewModel bindLockTypeSelectorViewModel(LockTypeSelectorViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PasswordSetViewModel.class)
    abstract ViewModel bindPasswordSetViewModel(PasswordSetViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(BiometricsSetViewModel.class)
    abstract ViewModel bindBiometricsSetViewModel(BiometricsSetViewModel viewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(NinjaViewModelFactory factory);
}
