package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.adapter.AccountListIteratorAdapter;
import me.gingerninja.authenticator.data.db.wrapper.AccountWrapper;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.CodeGenerator;

@Module
public class AccountListModule {
    @Provides
    AccountListIteratorAdapter provideAccountListIteratorAdapter(AccountWrapper.Factory accountWrapperFactory, CodeGenerator codeGenerator, AccountRepository accountRepository) {
        return new AccountListIteratorAdapter(accountWrapperFactory, codeGenerator, accountRepository);
    }
}
