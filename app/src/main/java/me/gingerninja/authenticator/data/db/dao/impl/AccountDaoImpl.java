package me.gingerninja.authenticator.data.db.dao.impl;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.requery.Persistable;
import io.requery.Transaction;
import io.requery.query.Tuple;
import io.requery.query.function.Case;
import io.requery.reactivex.ReactiveEntityStore;
import io.requery.reactivex.ReactiveResult;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.data.db.dao.AccountDao;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.AccountHasLabel;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.db.function.GroupConcat;
import me.gingerninja.authenticator.data.db.function.JsonObject;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;
import me.gingerninja.authenticator.data.db.wrapper.AccountWrapper;
import me.gingerninja.authenticator.data.db.wrapper.LabelWrapper;

public class AccountDaoImpl implements AccountDao {
    private final DatabaseHandler databaseHandler;

    public AccountDaoImpl(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    private ReactiveEntityStore<Persistable> getStore() {
        return databaseHandler.getEntityStore();
    }

    @Override
    public Single<Account> get(long id) {
        return getStore()
                .findByKey(Account.class, id)
                .toSingle();
    }

    @Override
    public Observable<List<Account>> getAllAndListen() {
        return getStore()
                .select(Account.class)
                .orderBy(Account.POSITION.asc())
                .get()
                .observableResult()
                .map(accounts -> accounts.observable().toList().blockingGet());
        /*return getStore()
                .select(Account.class)
                .orderBy(Account.POSITION.asc())
                .get()
                .observableResult()
                .map(accounts -> accounts.observable().toList().blockingGet());*/
    }

    @Override
    public Observable<ReactiveResult<Tuple>> getAccountsAndLabelsWithListen() {
        return getStore()
                .select(
                        Account.ID,
                        Account.TITLE,
                        Account.ACCOUNT_NAME,
                        Account.ISSUER,
                        Account.SECRET,
                        Account.TYPE,
                        Account.TYPE_SPECIFIC_DATA,
                        Account.ALGORITHM,
                        Account.DIGITS,
                        Account.SOURCE,
                        Account.POSITION,
                        Case.type(AccountWrapper.TUPLE_KEY_LABELS, String.class)
                                .when(Label.ID.isNull(), GroupConcat.groupConcat(Label.ID))
                                .elseThen(
                                        GroupConcat.groupConcat(
                                                JsonObject.create(LabelWrapper.FIELD_ID, Label.ID)
                                                        .add(LabelWrapper.FIELD_NAME, Label.NAME)
                                                        .add(LabelWrapper.FIELD_POSITION, AccountHasLabel.POSITION)
                                                        .add(LabelWrapper.FIELD_ICON, Label.ICON)
                                                        .add(LabelWrapper.FIELD_COLOR, Label.COLOR)
                                                        .jsonObject()
                                        )
                                )
                )
                .from(Account.class)
                .leftJoin(AccountHasLabel.class)
                .on(AccountHasLabel.ACCOUNT_ID.eq(Account.ID))
                .leftJoin(Label.class)
                .on(Label.ID.eq(AccountHasLabel.LABEL_ID))
                .groupBy(Account.ID)
                .orderBy(Account.POSITION)
                .get()
                .observableResult();
    }

    /*@Override
    public Observable<HashMap<Account, List<Label>>> getAllAndListen2() {
        // FIXME not good, it should be ordered by Account.POSITION

        return getStore()
                .select(AccountHasLabel.class)
                .orderBy(AccountHasLabel.POSITION)
                .get()
                .observableResult()
                .map(results -> results.observable()
                        .collectInto(new LinkedHashMap<Account, List<Label>>(), (accountListHashMap, accountHasLabel) -> {
                            //for (AccountHasLabel accountHasLabel : accountHasLabels) {
                            List<Label> list = accountListHashMap.get(accountHasLabel.getAccount());
                            if (list == null) {
                                list = new LinkedList<>();
                                accountListHashMap.put(accountHasLabel.getAccount(), list);
                            }

                            list.add(accountHasLabel.getLabel());
                            //}
                        })
                        .blockingGet());
    }*/

    @Override
    public Single<Account> save(Account account) {
        if (account.getPosition() < 0) {
            return getStore()
                    .count(Account.class)
                    .get()
                    .single()
                    .flatMap(cnt -> {
                        account.setPosition(cnt);
                        return getStore().upsert(account);
                    });
        } else {
            return getStore().upsert(account);
        }
    }

    @Override
    public Completable saveAll(final List<Account> accountList) {
        return getStore()
                .runInTransaction(db -> {
                    Transaction transaction = db.transaction();
                    if (!transaction.active()) {
                        transaction.begin();
                    }
                    try {
                        db.update(accountList);
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
    public Completable saveAccountOrder(int count, int from, int to, ResultSetIterator<Tuple> results) {
        return getStore()
                .runInTransaction(db -> {
                    Transaction transaction = db.transaction();
                    if (!transaction.active()) {
                        transaction.begin();
                    }
                    try {
                        int pos;
                        int min = Math.min(from, to);
                        int max = Math.max(from, to);
                        for (int i = 0; i < count; i++) {
                            if (i == from) {
                                pos = to;
                            } else if (i >= min && i <= max) {
                                if (from < to) {
                                    pos = i - 1;
                                } else {
                                    pos = i + 1;
                                }
                            } else {
                                pos = i;
                            }

                            Tuple tuple = results.get(i);
                            if (pos != tuple.get(Account.POSITION)) {
                                db.update(Account.class).set(Account.POSITION, pos).where(Account.ID.eq(tuple.get(Account.ID))).get().call();
                            }
                        }

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
    public Completable delete(Account account) {
        return getStore().delete(account);
    }
}
