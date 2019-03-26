package me.gingerninja.authenticator.data.db.dao;

import androidx.annotation.CheckResult;
import io.reactivex.Completable;
import me.gingerninja.authenticator.util.backup.Restore;

public interface TempDao {
    @CheckResult
    Completable clear();

    Completable loadJsonToDatabase(Restore.DatabaseProcessor processor);

    @CheckResult
    Completable restore();
}
