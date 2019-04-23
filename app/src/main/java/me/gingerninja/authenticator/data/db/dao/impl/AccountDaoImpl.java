package me.gingerninja.authenticator.data.db.dao.impl;

import android.os.Parcel;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.security.SecureRandom;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.BiPredicate;
import io.requery.Persistable;
import io.requery.Transaction;
import io.requery.query.MutableTuple;
import io.requery.query.NamedNumericExpression;
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

    @CheckResult
    @Override
    public Single<Account> get(long id) {
        return getStore()
                .findByKey(Account.class, id)
                .toSingle();
    }

    @CheckResult
    @Override
    public Single<Account> get(@NonNull String uid) {
        return getStore()
                .select(Account.class)
                .where(Account.UID.eq(uid))
                .get()
                .maybe()
                .toSingle();
    }

    @CheckResult
    @Override
    public Observable<List<Account>> getAllAndListen() {
        return getStore()
                .select(Account.class)
                .orderBy(Account.POSITION.asc())
                .get()
                .observableResult()
                .map(accounts -> accounts.observable().toList().blockingGet());
    }

    @CheckResult
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

    @CheckResult
    @Override
    public Single<Account> save(Account account) {
        if (account.getPosition() < 0) {
            return getStore()
                    .select(Account.POSITION.max().as("max"))
                    .from(Account.class)
                    .get()
                    .maybe()
                    .switchIfEmpty((SingleSource<? extends Tuple>) observer -> {
                        MutableTuple tuple = new MutableTuple(1);
                        tuple.set(0, new NamedNumericExpression<>("max", Integer.class), -1);
                        observer.onSuccess(tuple);
                    })
                    .map(tuple -> {
                        Integer t = tuple.get(0);
                        if (t == null) {
                            t = -1;
                        }
                        return t;
                    })
                    .flatMap(cnt -> {
                        account.setPosition(cnt + 1);
                        return saveWithRetry(account);//getStore().upsert(account);
                    });
        } else {
            return getStore().upsert(account);
        }
    }

    @CheckResult
    private Single<Account> saveWithRetry(@NonNull Account account) {
        Parcel parcel = account.writeToParcel();

        return getStore()
                .upsert(account)
                .retry(new BiPredicate<Integer, Throwable>() {
                    SecureRandom random = new SecureRandom();
                    byte[] bytes = new byte[8];

                    @Override
                    public boolean test(Integer retryCount, Throwable throwable) {

                        Throwable t = throwable.getCause();
                        boolean isUniqueError = t instanceof SQLIntegrityConstraintViolationException && t.getMessage().contains("UNIQUE");

                        if (isUniqueError) {
                            Account.restoreFromParcel(account, parcel);
                            random.nextBytes(bytes);
                            account.generateUID(bytes);
                        }

                        return retryCount < 10 && isUniqueError;
                    }
                })
                .doAfterTerminate(parcel::recycle);
    }

    @CheckResult
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

    @CheckResult
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

    @CheckResult
    @Override
    public Completable delete(Account account) {
        return getStore().delete(account);
    }
}
