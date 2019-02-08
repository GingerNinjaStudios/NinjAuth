package me.gingerninja.authenticator.data.db.dao.impl;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.requery.Persistable;
import io.requery.Transaction;
import io.requery.reactivex.ReactiveEntityStore;
import me.gingerninja.authenticator.data.db.dao.LabelDao;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.AccountHasLabel;
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
    public Observable<Label> getAll(long... exceptions) {
        List<Long> array = new ArrayList<>(exceptions.length);
        for (long ex : exceptions) {
            array.add(ex);
        }

        return getStore()
                .select(Label.class)
                .where(Label.ID.notIn(array))
                .orderBy(Label.POSITION.asc())
                .get()
                .observable();
    }

    @Override
    public Observable<AccountHasLabel> getLabelsByAccount(@NonNull Account account) {
        return getStore()
                .select(AccountHasLabel.class)
                .where(AccountHasLabel.ACCOUNT.eq(account))
                .orderBy(AccountHasLabel.POSITION.asc())
                .get()
                .observable();
    }

    @SuppressLint("CheckResult")
    @Override
    public Completable saveLabelsForAccount(@NonNull Account account, @NonNull List<Label> labels) {
        final List<AccountHasLabel> accountHasLabelList = new ArrayList<>(labels.size());
        final List<Long> labelIdList = new ArrayList<>(labels.size());

        int i = 0;
        for (Label label : labels) {
            AccountHasLabel accountHasLabel = new AccountHasLabel();
            accountHasLabel.setAccount(account);
            accountHasLabel.setLabel(label);
            accountHasLabel.setPosition(i++);
            accountHasLabelList.add(accountHasLabel);

            labelIdList.add(label.getId());
        }

        return getStore()
                .runInTransaction(db -> {
                    Transaction transaction = db.transaction();

                    if (!db.transaction().active()) {
                        transaction.begin();
                    }

                    try {
                        db.delete(AccountHasLabel.class)
                                .where(AccountHasLabel.ACCOUNT_ID.eq(account.getId())
                                        .and(AccountHasLabel.LABEL_ID.notIn(labelIdList)))
                                .get()
                                .call();

                        db.upsert(accountHasLabelList);

                        // FIXME temporary fix of requery bug: https://github.com/requery/requery/issues/854
                        for (AccountHasLabel hasLabel : accountHasLabelList) {
                            hasLabel.setPosition(hasLabel.getPosition());
                        }
                        db.update(accountHasLabelList);
                        // end of fix

                        transaction.commit();
                    } catch (Throwable t) {
                        transaction.rollback();
                        throw new RuntimeException(t);
                    }

                    transaction.close();

                    return true;
                })
                .ignoreElement();
    }

    @Override
    public Observable<List<Label>> getAllAndListen() {
        return getStore()
                .select(Label.class)
                .orderBy(Label.POSITION.asc())
                .get()
                .observableResult()
                .map(accounts -> accounts.observable().toList().blockingGet());
    }

    @Override
    public Single<Label> save(Label label) {
        if (label.getPosition() < 0) {
            return getStore()
                    .count(Label.class)
                    .get()
                    .single()
                    .flatMap(cnt -> {
                        label.setPosition(cnt);
                        return getStore().upsert(label);
                    });
        } else {
            return getStore().upsert(label);
        }
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
