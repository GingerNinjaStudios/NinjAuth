package me.gingerninja.authenticator.di.module;


import androidx.lifecycle.ViewModelProvider;
import dagger.Binds;
import dagger.Module;
import me.gingerninja.authenticator.viewmodel.NinjaViewModelFactory;

@Module
public abstract class ViewModelModule {
    /*@Binds
    @IntoMap
    @ViewModelKey(MyViewModel.class)
    abstract ViewModel bindUserViewModel(MyViewModel myViewModel);*/

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(NinjaViewModelFactory factory);
}
