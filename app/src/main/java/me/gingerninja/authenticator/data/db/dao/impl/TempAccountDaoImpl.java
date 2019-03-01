package me.gingerninja.authenticator.data.db.dao.impl;

import io.reactivex.Completable;
import io.requery.Persistable;
import io.requery.Transaction;
import io.requery.reactivex.ReactiveEntityStore;
import me.gingerninja.authenticator.data.db.dao.TempAccountDao;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.TempAccount;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;

public class TempAccountDaoImpl implements TempAccountDao {
    private final DatabaseHandler databaseHandler;

    public TempAccountDaoImpl(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    private ReactiveEntityStore<Persistable> getStore() {
        return databaseHandler.getEntityStore();
    }

    @Override
    public Completable copyAccounts(long[] accountIds, long[] labelIds) {
        return getStore()
                .runInTransaction(db -> {
                    Transaction transaction = db.transaction();
                    if (!transaction.active()) {
                        transaction.begin();
                    }
                    try {
                        // TODO not good, some will need to be inserted, some updated, etc.
                        db.insert(Account.class, Account.ID, Account.UID)
                                .query(
                                        db.select(TempAccount.class, TempAccount.ID, TempAccount.UID)
                                )
                                .get()
                                .close();
                        //db.update(accountList);
                        transaction.commit();
                    } catch (Throwable t) {
                        transaction.rollback();
                        throw new RuntimeException(t);
                    }
                    return true;
                })
                .ignoreElement();
    }
}
