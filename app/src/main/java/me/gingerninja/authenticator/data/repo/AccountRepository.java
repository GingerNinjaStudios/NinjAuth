package me.gingerninja.authenticator.data.repo;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import me.gingerninja.authenticator.data.db.dao.AccountDao;
import me.gingerninja.authenticator.data.db.dao.CategoryDao;
import me.gingerninja.authenticator.data.db.entity.Account;

@Singleton
public class AccountRepository {
    private final AccountDao accountDao;
    private final CategoryDao categoryDao;

    @Inject
    public AccountRepository(AccountDao accountDao, CategoryDao categoryDao) {
        this.accountDao = accountDao;
        this.categoryDao = categoryDao;
    }

    public Single<Account> addAccount(Account account) {
        return accountDao.save(account);
    }

    public Observable<List<Account>> getAll() {
        return accountDao.getAll();
    }
}
