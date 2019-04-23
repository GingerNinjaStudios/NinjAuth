package me.gingerninja.authenticator.data.db.dao.impl;

import android.os.Parcel;

import java.security.SecureRandom;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
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

    @CheckResult
    @Override
    public Single<Label> get(long id) {
        return getStore()
                .findByKey(Label.class, id)
                .toSingle();
    }

    @CheckResult
    @Override
    public Single<Label> get(String uid) {
        return getStore()
                .select(Label.class)
                .where(Label.UID.eq(uid))
                .get()
                .maybe()
                .toSingle();
    }

    @CheckResult
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

    @CheckResult
    @Override
    public Observable<AccountHasLabel> getLabelsByAccount(@NonNull Account account) {
        return getStore()
                .select(AccountHasLabel.class)
                .where(AccountHasLabel.ACCOUNT.eq(account))
                .orderBy(AccountHasLabel.POSITION.asc())
                .get()
                .observable();
    }

    @CheckResult
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

                    db.update(account); // this is basically no-op but it refreshes the accounts list

                    return true;
                })
                .ignoreElement();
    }

    @CheckResult
    @Override
    public Observable<List<Label>> getAllAndListen() {
        return getStore()
                .select(Label.class)
                .orderBy(Label.POSITION.asc())
                .get()
                .observableResult()
                .map(accounts -> accounts.observable().toList().blockingGet());
    }

    @CheckResult
    @Override
    public Single<Label> save(Label label) {
        if (label.getPosition() < 0) {
            return getStore()
                    .select(Label.POSITION.max().as("max"))
                    .from(Label.class)
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
                        label.setPosition(cnt + 1);
                        return saveWithRetry(label);//getStore().upsert(label);
                    });
        } else {
            return getStore().upsert(label);
        }
    }

    @CheckResult
    private Single<Label> saveWithRetry(@NonNull Label label) {
        Parcel parcel = label.writeToParcel();

        return getStore()
                .upsert(label)
                .retry(new BiPredicate<Integer, Throwable>() {
                    SecureRandom random = new SecureRandom();
                    byte[] bytes = new byte[8];

                    @Override
                    public boolean test(Integer retryCount, Throwable throwable) {

                        Throwable t = throwable.getCause();
                        boolean isUniqueError = t instanceof SQLIntegrityConstraintViolationException && t.getMessage().contains("UNIQUE");

                        if (isUniqueError) {
                            Label.restoreFromParcel(label, parcel);
                            random.nextBytes(bytes);
                            label.generateUID(bytes);
                        }

                        return retryCount < 10 && isUniqueError;
                    }
                })
                .doAfterTerminate(parcel::recycle);
    }

    @CheckResult
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

    @CheckResult
    @Override
    public Completable delete(Label label) {
        return getStore().delete(label);
    }
}
