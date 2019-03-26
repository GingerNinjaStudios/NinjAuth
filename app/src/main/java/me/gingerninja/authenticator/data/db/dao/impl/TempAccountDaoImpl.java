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
import me.gingerninja.authenticator.data.db.dao.TempAccountDao;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.AccountHasLabel;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.db.entity.TempAccount;
import me.gingerninja.authenticator.data.db.entity.TempAccountHasLabel;
import me.gingerninja.authenticator.data.db.entity.TempLabel;
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
    public Completable clear() {
        return Completable.concatArray(
                getStore().delete(TempAccount.class).get().single().ignoreElement(),
                getStore().delete(TempLabel.class).get().single().ignoreElement(),
                getStore().delete(TempAccountHasLabel.class).get().single().ignoreElement()
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
                                .where(TempAccount.RESTORE_MODE.eq(TempAccount.MODE_INSERT))
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
                                .where(TempAccount.RESTORE_MODE.eq(TempAccount.MODE_UPDATE))
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
                                .where(TempLabel.RESTORE_MODE.eq(TempLabel.MODE_INSERT))
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
                                .where(TempLabel.RESTORE_MODE.eq(TempLabel.MODE_UPDATE))
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

                                db.upsert(accountHasLabel);
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
