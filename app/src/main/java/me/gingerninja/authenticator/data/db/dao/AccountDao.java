package me.gingerninja.authenticator.data.db.dao;

import java.util.List;

import androidx.annotation.CheckResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import me.gingerninja.authenticator.data.db.entity.Account;

public interface AccountDao {
    @CheckResult
    Single<Account> get(long id);

    @CheckResult
    Observable<List<Account>> getAllAndListen();

    @CheckResult
    Single<Account> save(Account account);

    @CheckResult
    Completable saveAll(List<Account> accountList);

    @CheckResult
    Completable delete(Account account);
}
