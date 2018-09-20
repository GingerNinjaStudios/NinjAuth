package me.gingerninja.authenticator.di.module;


import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import me.gingerninja.authenticator.di.ViewModelKey;
import me.gingerninja.authenticator.ui.account.camera.AddAccountFromCameraViewModel;
import me.gingerninja.authenticator.ui.home.AccountListViewModel;
import me.gingerninja.authenticator.viewmodel.NinjaViewModelFactory;

@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AccountListViewModel.class)
    abstract ViewModel bindAccountListViewModel(AccountListViewModel myViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AddAccountFromCameraViewModel.class)
    abstract ViewModel bindAddAccountFromCameraViewModel(AddAccountFromCameraViewModel myViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(NinjaViewModelFactory factory);
}
