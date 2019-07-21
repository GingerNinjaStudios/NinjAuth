package me.gingerninja.authenticator.data.repo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.requery.query.Tuple;
import io.requery.reactivex.ReactiveResult;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.data.db.dao.AccountDao;
import me.gingerninja.authenticator.data.db.dao.LabelDao;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.AccountHasLabel;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.ui.home.filter.AccountFilterObject;

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

    public Completable saveLabelsForAccount(Account account, List<Label> labels) {
        return labelDao.saveLabelsForAccount(account, labels);
    }

    public Single<Account> getAccount(long id) {
        return accountDao.get(id);
    }

    public Observable<List<Account>> getAllAccountAndListen() {
        return accountDao.getAllAndListen();
    }

    public Observable<Account> getAccounts() {
        return accountDao.getAll();
    }

    public Observable<ReactiveResult<Tuple>> getAllAccountAndListen2(@Nullable AccountFilterObject filterObject) {
        return accountDao.getAccountsAndLabelsWithListen(filterObject);
        /*return accountDao.getAllAndListen()
                .map(accounts -> Observable.fromIterable(accounts)
                        .map(account -> new Pair<>(account, labelDao.getLabelsByAccount2(account)
                                .toList()
                                .blockingGet())
                        )
                        .collectInto(new LinkedHashMap<Account, List<Label>>(), (linkedHashMap, accountListPair) -> linkedHashMap.put(accountListPair.first, accountListPair.second))
                        .blockingGet()
                );*/
        //return accountDao.getAllAndListen2();
    }

    public Completable saveAccountOrder(int count, int from, int to, ResultSetIterator<Tuple> results) {
        return accountDao.saveAccountOrder(count, from, to, results);
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

    public Single<Integer> getFilteredAccountCount(@NonNull AccountFilterObject filterObject) {
        return accountDao.getFilteredAccountCount(filterObject).subscribeOn(Schedulers.io());
    }

    public Single<Label> addLabel(Label label) {
        return labelDao.save(label);
    }

    public Single<Label> getLabel(long id) {
        return labelDao.get(id);
    }

    public Observable<Label> getAllLabel(long... exceptions) {
        return labelDao.getAll(exceptions);
    }

    public Observable<AccountHasLabel> getLabelsByAccount(@NonNull Account account) {
        return labelDao.getLabelsByAccount(account);
    }

    public Observable<List<Label>> getAllLabelAndListen() {
        return labelDao.getAllAndListen();
    }

    public Completable deleteLabel(long labelId) {
        return getLabel(labelId).flatMapCompletable(labelDao::delete);
    }

    public Completable deleteLabel(Label label) {
        return labelDao.delete(label);
    }

    public Maybe<Account> findExistingAccount(@NonNull Account account) {
        return accountDao.findExistingAccount(account);
    }
}
