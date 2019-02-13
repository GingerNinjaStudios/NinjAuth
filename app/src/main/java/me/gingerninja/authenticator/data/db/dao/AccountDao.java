package me.gingerninja.authenticator.data.db.dao;

import java.util.List;

import androidx.annotation.CheckResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.requery.query.Tuple;
import io.requery.reactivex.ReactiveResult;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.data.db.entity.Account;

public interface AccountDao {
    @CheckResult
    Single<Account> get(long id);

    @CheckResult
    Observable<List<Account>> getAllAndListen();

    Observable<ReactiveResult<Tuple>> getAccountsAndLabelsWithListen();

    @CheckResult
    Single<Account> save(Account account);

    @CheckResult
    Completable saveAll(List<Account> accountList);

    Completable saveAccountOrder(int count, int from, int to, ResultSetIterator<Tuple> results);

    @CheckResult
    Completable delete(Account account);
}
