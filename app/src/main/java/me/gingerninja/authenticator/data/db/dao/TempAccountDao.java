package me.gingerninja.authenticator.data.db.dao;

import io.reactivex.Completable;

public interface TempAccountDao {
    Completable copyAccounts(long[] accountIds, long[] labelIds);
}
