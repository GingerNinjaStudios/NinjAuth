package me.gingerninja.authenticator.di.module;


import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import me.gingerninja.authenticator.di.ViewModelKey;
import me.gingerninja.authenticator.ui.account.camera.AddAccountFromCameraViewModel;
import me.gingerninja.authenticator.ui.backup.RestoreViewModel;
import me.gingerninja.authenticator.ui.home.AccountListViewModel;
import me.gingerninja.authenticator.ui.home.DeleteAccountViewModel;
import me.gingerninja.authenticator.ui.home.form.AccountEditorViewModel;
import me.gingerninja.authenticator.ui.label.LabelsViewModel;
import me.gingerninja.authenticator.ui.label.form.LabelEditorViewModel;
import me.gingerninja.authenticator.ui.setup.SetupViewModel;
import me.gingerninja.authenticator.viewmodel.NinjaViewModelFactory;

@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SetupViewModel.class)
    abstract ViewModel bindSetupViewModel(SetupViewModel viewModel);

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
    @ViewModelKey(AccountEditorViewModel.class)
    abstract ViewModel bindAccountEditorViewModel(AccountEditorViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DeleteAccountViewModel.class)
    abstract ViewModel bindDeleteAccountViewModel(DeleteAccountViewModel myViewModel);

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
    @ViewModelKey(RestoreViewModel.class)
    abstract ViewModel bindRestoreViewModel(RestoreViewModel viewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(NinjaViewModelFactory factory);
}
