package me.gingerninja.authenticator.data.db.dao.impl;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.requery.Persistable;
import io.requery.Transaction;
import io.requery.reactivex.ReactiveEntityStore;
import me.gingerninja.authenticator.data.db.dao.LabelDao;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;

public class LabelDaoImpl implements LabelDao {
    private final DatabaseHandler databaseHandler;

    public LabelDaoImpl(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    private ReactiveEntityStore<Persistable> getStore() {
        return databaseHandler.getEntityStore();
    }

    @Override
    public Single<Label> get(long id) {
        return getStore()
                .findByKey(Label.class, id)
                .toSingle();
    }

    @Override
    public Observable<Label> getAll() {
        return getStore()
                .select(Label.class)
                .orderBy(Label.POSITION.asc())
                .get()
                .observable();
    }

    @Override
    public Single<Label> save(Label label) {
        return getStore().upsert(label);
    }

    @Override
    public Completable saveAll(List<Label> labelList) {
        return getStore()
                .runInTransaction(db -> {
                    Transaction transaction = db.transaction();
                    if (!transaction.active()) {
                        transaction.begin();
                    }
                    try {
                        db.update(labelList);
                        transaction.commit();
                    } catch (Throwable t) {
                        transaction.rollback();
                        throw new RuntimeException(t);
                    }
                    return true;
                })
                .ignoreElement();
    }

    @Override
    public Completable delete(Label label) {
        return getStore().delete(label);
    }
}
