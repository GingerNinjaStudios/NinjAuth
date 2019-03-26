package me.gingerninja.authenticator.data.db.dao;

import androidx.annotation.CheckResult;
import io.reactivex.Completable;

public interface TempAccountDao {
    @CheckResult
    Completable clear();

    @CheckResult
    Completable restore();
}
