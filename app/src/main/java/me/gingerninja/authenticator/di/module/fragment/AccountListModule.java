package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.adapter.AccountListAdapter;
import me.gingerninja.authenticator.util.CodeGenerator;

@Module
public class AccountListModule {
    @Provides
    AccountListAdapter provideAccountListAdapter(CodeGenerator codeGenerator) {
        return new AccountListAdapter(codeGenerator);
    }
}
