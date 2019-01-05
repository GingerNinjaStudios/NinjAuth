package me.gingerninja.authenticator.data.repo;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import me.gingerninja.authenticator.data.db.dao.AccountDao;
import me.gingerninja.authenticator.data.db.dao.LabelDao;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;

@Singleton
public class AccountRepository {
    private final AccountDao accountDao;
    private final LabelDao labelDao;

    @Inject
    public AccountRepository(AccountDao accountDao, LabelDao labelDao) {
        this.accountDao = accountDao;
        this.labelDao = labelDao;
    }

    public Single<Account> addAccount(Account account) {
        return accountDao.save(account);
    }

    public Single<Account> getAccount(long id) {
        return accountDao.get(id);
    }

    public Observable<List<Account>> getAllAccountAndListen() {
        return accountDao.getAllAndListen();
    }

    public Completable saveAccounts(List<Account> accountList) {
        return accountDao.saveAll(accountList);
    }

    public Completable deleteAccount(long accountId) {
        return getAccount(accountId).flatMapCompletable(accountDao::delete);
    }

    public Completable deleteAccount(Account account) {
        return accountDao.delete(account);
    }

    public Single<Label> addLabel(Label label) {
        return labelDao.save(label);
    }

    public Single<Label> getLabel(long id) {
        return labelDao.get(id);
    }

    public Observable<Label> getAllLabel() {
        return labelDao.getAll();
    }

    public Completable deleteLabel(Label label) {
        return labelDao.delete(label);
    }
}
