package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.ui.home.filter.AccountFilterLabelAdapter;

@Module
public class AccountFilterModule {
    @Provides
    AccountFilterLabelAdapter provideAccountFilterLabelAdapter() {
        return new AccountFilterLabelAdapter();
    }
}
