package me.gingerninja.authenticator.data.db.dao;

import androidx.annotation.CheckResult;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.requery.query.Tuple;
import io.requery.reactivex.ReactiveResult;
import me.gingerninja.authenticator.data.db.entity.TempAccount;
import me.gingerninja.authenticator.data.db.entity.TempLabel;
import me.gingerninja.authenticator.util.backup.Restore;

public interface TempDao {
    @CheckResult
    Completable clear();

    @CheckResult
    Completable loadJsonToDatabase(Restore.DatabaseProcessor processor);

    @CheckResult
    Completable restore();

    @CheckResult
    Completable updateAccountRestoreStatus(long id, boolean shouldRestore);

    @CheckResult
    Completable updateAccountRestoreMode(long id, @TempAccount.RestoreMode int mode);

    @CheckResult
    Completable updateLabelRestoreStatus(long id, boolean shouldRestore);

    @CheckResult
    Completable updateLabelRestoreMode(long id, @TempLabel.RestoreMode int mode);

    Observable<ReactiveResult<Tuple>> getAccounts();

    Observable<ReactiveResult<Tuple>> getLabels();
}
