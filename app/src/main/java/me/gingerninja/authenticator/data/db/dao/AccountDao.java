package me.gingerninja.authenticator.data.db.dao;

import java.util.List;

import androidx.annotation.CheckResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import me.gingerninja.authenticator.data.db.entity.Account;

public interface AccountDao {
    @CheckResult
    Observable<List<Account>> getAll();

    @CheckResult
    Single<Account> save(Account account);

    @CheckResult
    Completable delete(Account account);
}
