package me.gingerninja.authenticator.di.builder;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.gingerninja.authenticator.ui.account.camera.AddAccountFromCameraFragment;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.AccountListFragment;

@Module
public abstract class FragmentBuilder {
    /*@ContributesAndroidInjector
    abstract BaseFragment bindBaseFragment();*/

    @ContributesAndroidInjector
    abstract AccountListFragment bindAccountListFragment();

    @ContributesAndroidInjector
    abstract AddAccountFromCameraFragment bindAddAccountFromCameraFragment();
}
