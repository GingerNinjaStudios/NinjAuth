package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.adapter.AccountListAdapter;
import me.gingerninja.authenticator.data.adapter.AccountListIteratorAdapter;
import me.gingerninja.authenticator.data.db.wrapper.AccountWrapper;
import me.gingerninja.authenticator.util.CodeGenerator;

@Module
public class AccountListModule {
    @Provides
    AccountListAdapter provideAccountListAdapter(CodeGenerator codeGenerator) {
        return new AccountListAdapter(codeGenerator);
    }

    @Provides
    AccountListIteratorAdapter provideAccountListIteratorAdapter(AccountWrapper.Factory accountWrapperFactory, CodeGenerator codeGenerator) {
        return new AccountListIteratorAdapter(accountWrapperFactory, codeGenerator);
    }
}
