package me.gingerninja.authenticator.data.db.dao.impl;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.requery.Persistable;
import io.requery.Transaction;
import io.requery.query.MutableTuple;
import io.requery.query.NamedNumericExpression;
import io.requery.query.Tuple;
import io.requery.reactivex.ReactiveEntityStore;
import io.requery.util.CloseableIterator;
import me.gingerninja.authenticator.data.db.dao.TempDao;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.AccountHasLabel;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.db.entity.TempAccount;
import me.gingerninja.authenticator.data.db.entity.TempAccountHasLabel;
import me.gingerninja.authenticator.data.db.entity.TempLabel;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;
import me.gingerninja.authenticator.data.pojo.BackupAccount;
import me.gingerninja.authenticator.data.pojo.BackupLabel;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.util.backup.Restore;
import timber.log.Timber;

public class TempDaoImpl implements TempDao {
    private final DatabaseHandler databaseHandler;

    public TempDaoImpl(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    private ReactiveEntityStore<Persistable> getStore() {
        return databaseHandler.getEntityStore();
    }

    @Override
    public Completable clear() {
        return Completable.concatArray(
                Completable.defer(() -> {
                    try {
                        getStore().delete(TempAccount.class).get().single().ignoreElement().blockingAwait();
                    } catch (Throwable ignored) {
                        // the table did not exist probably
                    }
                    return Completable.complete();
                }),
                Completable.defer(() -> {
                    try {
                        getStore().delete(TempLabel.class).get().single().ignoreElement().blockingAwait();
                    } catch (Throwable ignored) {
                        // the table did not exist probably
                    }
                    return Completable.complete();
                }),
                Completable.defer(() -> {
                    try {
                        getStore().delete(TempAccountHasLabel.class).get().single().ignoreElement().blockingAwait();
                    } catch (Throwable ignored) {
                        // the table did not exist probably
                    }
                    return Completable.complete();
                })
                //getStore().delete(TempAccount.class).get().single().ignoreElement(),
                //getStore().delete(TempLabel.class).get().single().ignoreElement(),
                //getStore().delete(TempAccountHasLabel.class).get().single().ignoreElement()
        );
    }

    @Override
    public Completable loadJsonToDatabase(Restore.DatabaseProcessor processor) {
        return clear()
                .andThen(
                        Completable.defer(() -> getStore()
                                .runInTransaction(db -> {
                                    TemporaryRepository.RestoreHandler restoreHandler = new TemporaryRepository.RestoreHandler() {
                                        @Override
                                        public void addAccount(BackupAccount backupAccount) {
                                            TempAccount inserted = backupAccount.toEntity();

                                            Account existingAccount = db.select(Account.class)
                                                    .where(Account.UID.eq(inserted.getUid()))
                                                    .get()
                                                    .firstOrNull();

                                            if (existingAccount != null) {
                                                inserted.setRestore(!inserted.equalsToAccount(existingAccount));
                                                inserted.setRestoreMode(TempAccount.RestoreMode.UPDATE);
                                                inserted.setRestoreMatchingUid(existingAccount.getUid());
                                            }

                                            db.insert(inserted);

                                            if (backupAccount.getLabelIds() != null) {
                                                String[] labelIds = backupAccount.getLabelIds();
                                                for (int i = 0; i < labelIds.length; i++) {
                                                    String labelUid = labelIds[i];
                                                    // this should not fail because the defer_foreign_keys pragma is ON
                                                    db.insert(TempAccountHasLabel.class)
                                                            .value(TempAccountHasLabel.ACCOUNT_ID, inserted.getUid())
                                                            .value(TempAccountHasLabel.LABEL_ID, labelUid)
                                                            .value(TempAccountHasLabel.POSITION, i)
                                                            .get()
                                                            .close();
                                                }
                                            }
                                        }

                                        @Override
                                        public void addLabel(BackupLabel backupLabel) {
                                            TempLabel inserted = backupLabel.toEntity();

                                            Label existingLabel = db.select(Label.class)
                                                    .where(Label.UID.eq(inserted.getUid()))
                                                    .get()
                                                    .firstOrNull();

                                            if (existingLabel != null) {
                                                inserted.setRestore(!inserted.equalsToLabel(existingLabel));
                                                inserted.setRestoreMode(TempLabel.RestoreMode.UPDATE);
                                                inserted.setRestoreMatchingUid(existingLabel.getUid());
                                            }

                                            db.insert(inserted);
                                        }
                                    };

                                    Transaction transaction = db.transaction();
                                    if (!transaction.active()) {
                                        transaction.begin();
                                    }
                                    try {
                                        db.raw("PRAGMA defer_foreign_keys = ON").close(); // defer foreign key check until the end of transaction

                                        Timber.v("[DB] Temporary restore process starting");
                                        processor.process(restoreHandler);
                                        Timber.v("[DB] Temporary restore process done");

                                        transaction.commit();
                                    } catch (Throwable t) {
                                        Timber.e(t, "Cannot finish temporary restore process: %s", t.getMessage());
                                        transaction.rollback();
                                        throw new RuntimeException(t);
                                    }

                                    return true;
                                })
                                .ignoreElement()
                        )
                );
    }

    @Override
    public Completable restore() {
        return getStore()
                .runInTransaction(db -> {
                    Transaction transaction = db.transaction();
                    if (!transaction.active()) {
                        transaction.begin();
                    }
                    try {
                        // delete what we don't restore
                        db.delete(TempAccount.class).where(TempAccount.RESTORE.eq(false)).get().call();
                        db.delete(TempLabel.class).where(TempLabel.RESTORE.eq(false)).get().call();

                        // insert accounts
                        CloseableIterator<TempAccount> accountIterator = db
                                .select(TempAccount.class)
                                .where(TempAccount.RESTORE_MODE.eq(TempAccount.RestoreMode.INSERT))
                                .get()
                                .iterator();

                        while (accountIterator.hasNext()) {
                            TempAccount tempAccount = accountIterator.next();
                            Account account = new Account();
                            setupAccount(account, tempAccount);
                            db.insert(account);
                        }

                        accountIterator.close();

                        // update accounts
                        accountIterator = db
                                .select(TempAccount.class)
                                .where(TempAccount.RESTORE_MODE.eq(TempAccount.RestoreMode.UPDATE))
                                .get()
                                .iterator();

                        while (accountIterator.hasNext()) {
                            TempAccount tempAccount = accountIterator.next();
                            Account account = db.select(Account.class)
                                    .where(Account.UID.eq(tempAccount.getUid()))
                                    .get()
                                    .firstOr(new Account());
                            setupAccount(account, tempAccount);
                            db.upsert(account);
                        }

                        accountIterator.close();

                        // insert labels
                        CloseableIterator<TempLabel> labelIterator = db
                                .select(TempLabel.class)
                                .where(TempLabel.RESTORE_MODE.eq(TempLabel.RestoreMode.INSERT))
                                .get()
                                .iterator();

                        while (labelIterator.hasNext()) {
                            TempLabel tempLabel = labelIterator.next();
                            Label label = new Label();
                            setupLabel(label, tempLabel);
                            db.insert(label);
                        }

                        labelIterator.close();

                        // update labels
                        labelIterator = db
                                .select(TempLabel.class)
                                .where(TempLabel.RESTORE_MODE.eq(TempLabel.RestoreMode.UPDATE))
                                .get()
                                .iterator();

                        while (labelIterator.hasNext()) {
                            TempLabel tempLabel = labelIterator.next();
                            Label label = db.select(Label.class)
                                    .where(Label.UID.eq(tempLabel.getUid()))
                                    .get()
                                    .firstOr(new Label());
                            setupLabel(label, tempLabel);
                            db.upsert(label);
                        }

                        labelIterator.close();

                        CloseableIterator<TempAccountHasLabel> itLinks = db.select(TempAccountHasLabel.class)
                                .orderBy(TempAccountHasLabel.ACCOUNT_ID.asc(), TempAccountHasLabel.POSITION.asc())
                                .get()
                                .iterator();

                        // TODO cache position for account maybe?

                        while (itLinks.hasNext()) {
                            TempAccountHasLabel temp = itLinks.next();
                            Account account = db.select(Account.class).where(Account.UID.eq(temp.getAccount().getUid())).get().firstOrNull();
                            Label label = db.select(Label.class).where(Label.UID.eq(temp.getLabel().getUid())).get().firstOrNull();

                            if (account != null && label != null) {
                                Tuple maxTuple = db.select(AccountHasLabel.POSITION.max().as("max"))
                                        .where(AccountHasLabel.ACCOUNT_ID.eq(account.getId()))
                                        .get()
                                        .firstOr(() -> {
                                            MutableTuple tuple = new MutableTuple(1);
                                            tuple.set(0, new NamedNumericExpression<>("max", Integer.class), -1);
                                            return tuple;
                                        });

                                Integer pos = maxTuple.get(0);
                                if (pos == null) {
                                    pos = -1;
                                }

                                AccountHasLabel accountHasLabel = new AccountHasLabel()
                                        .setAccount(account)
                                        .setLabel(label)
                                        .setPosition(pos + 1);

                                accountHasLabel = db.upsert(accountHasLabel);
                                // FIXME temporary fix of requery bug: https://github.com/requery/requery/issues/854
                                accountHasLabel.setPosition(pos + 1);
                                db.update(accountHasLabel);
                            }
                        }

                        itLinks.close();

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
    public Completable updateAccountRestoreStatus(long id, boolean shouldRestore) {
        return getStore()
                .findByKey(TempAccount.class, id)
                .flatMapSingle(tempAccount -> {
                    tempAccount.setRestore(shouldRestore);
                    return getStore().update(tempAccount);
                })
                .ignoreElement();
    }

    @Override
    public Completable updateAccountRestoreMode(long id, @TempAccount.RestoreMode int mode) {
        return getStore()
                .findByKey(TempAccount.class, id)
                .flatMapSingle(tempAccount -> {
                    tempAccount.setRestoreMode(mode);
                    return getStore().update(tempAccount);
                })
                .ignoreElement();
    }

    @Override
    public Completable updateLabelRestoreStatus(long id, boolean shouldRestore) {
        return getStore()
                .findByKey(TempLabel.class, id)
                .flatMapSingle(tempLabel -> {
                    tempLabel.setRestore(shouldRestore);
                    return getStore().update(tempLabel);
                })
                .ignoreElement();
    }

    @Override
    public Completable updateLabelRestoreMode(long id, @TempLabel.RestoreMode int mode) {
        return getStore()
                .findByKey(TempLabel.class, id)
                .flatMapSingle(tempLabel -> {
                    tempLabel.setRestoreMode(mode);
                    return getStore().update(tempLabel);
                })
                .ignoreElement();
    }

    private void setupAccount(@NonNull Account account, @NonNull TempAccount tempAccount) {
        account.setTitle(tempAccount.getTitle());
        account.setAccountName(tempAccount.getAccountName());
        account.setIssuer(tempAccount.getIssuer());
        account.setSecret(tempAccount.getSecret());
        account.setDigits(tempAccount.getDigits());
        account.setType(tempAccount.getType());
        account.setTypeSpecificData(tempAccount.getTypeSpecificData());
        account.setAlgorithm(tempAccount.getAlgorithm());
        account.setSource(tempAccount.getSource());
        account.setUid(tempAccount.getUid());
        account.setPosition(tempAccount.getPosition());
    }

    private void setupLabel(@NonNull Label label, @NonNull TempLabel tempLabel) {
        label.setName(tempLabel.getName());
        label.setColor(tempLabel.getColor());
        label.setIcon(tempLabel.getIcon());
        label.setUid(tempLabel.getUid());
        label.setPosition(tempLabel.getPosition());
    }

    /*Long[] ids = new Long[options.insertAccountIds.length];
                            for (int i = 0, insertAccountIdsLength = options.insertAccountIds.length; i < insertAccountIdsLength; i++) {
                                ids[i] = options.insertAccountIds[i];

                            }
                            WindowedArrayList<Long> insertAccountList = new WindowedArrayList<>(ids, MAX_IN_FILTER);

                            while (insertAccountList.canMove()) {
                                db.insert(Account.class,
                                        Account.ID,
                                        Account.UID,
                                        Account.ACCOUNT_NAME,
                                        Account.ISSUER,
                                        Account.TITLE,
                                        Account.ALGORITHM,
                                        Account.DIGITS,
                                        Account.TYPE,
                                        Account.TYPE_SPECIFIC_DATA,
                                        Account.POSITION,
                                        Account.SECRET,
                                        Account.SOURCE
                                )
                                        .query(
                                                db.select(TempAccount.class,
                                                        TempAccount.ID,
                                                        TempAccount.UID,
                                                        TempAccount.ACCOUNT_NAME,
                                                        TempAccount.ISSUER,
                                                        TempAccount.TITLE,
                                                        TempAccount.ALGORITHM,
                                                        TempAccount.DIGITS,
                                                        TempAccount.TYPE,
                                                        TempAccount.TYPE_SPECIFIC_DATA,
                                                        TempAccount.POSITION,
                                                        TempAccount.SECRET,
                                                        TempAccount.SOURCE
                                                )
                                                        .where(TempAccount.ID.in(insertAccountList))
                                        )
                                        .get()
                                        .close();

                                insertAccountList.move();
                            }*/
}
