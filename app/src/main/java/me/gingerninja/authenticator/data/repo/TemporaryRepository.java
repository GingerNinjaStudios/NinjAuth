package me.gingerninja.authenticator.data.repo;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.requery.query.Tuple;
import io.requery.reactivex.ReactiveResult;
import me.gingerninja.authenticator.data.db.dao.TempDao;
import me.gingerninja.authenticator.data.db.entity.TempAccount;
import me.gingerninja.authenticator.data.db.entity.TempLabel;
import me.gingerninja.authenticator.data.pojo.BackupAccount;
import me.gingerninja.authenticator.data.pojo.BackupLabel;
import me.gingerninja.authenticator.util.backup.Restore;

@Singleton
public class TemporaryRepository {
    private final TempDao tempDao;

    @Inject
    TemporaryRepository(TempDao tempDao) {
        this.tempDao = tempDao;
    }

    public Completable prepare() {
        return tempDao.clear();
    }

    public Completable preprocessRestore(Restore.DatabaseProcessor databaseProcessor) {
        return tempDao.loadJsonToDatabase(databaseProcessor);
    }

    public Completable restoreAndPurge() {
        return tempDao
                .restore()
                .andThen(Completable.defer(tempDao::clear));
    }

    public Completable setAccountRestoreEnabled(long id, boolean shouldRestore) {
        return tempDao.updateAccountRestoreStatus(id, shouldRestore);
    }

    public Completable setAccountRestoreMode(long id, @TempAccount.RestoreMode int mode) {
        return tempDao.updateAccountRestoreMode(id, mode);
    }

    public Completable setLabelRestoreEnabled(long id, boolean shouldRestore) {
        return tempDao.updateLabelRestoreStatus(id, shouldRestore);
    }

    public Completable setLabelRestoreMode(long id, @TempLabel.RestoreMode int mode) {
        return tempDao.updateLabelRestoreMode(id, mode);
    }

    public Observable<ReactiveResult<Tuple>> getAccounts() {
        return tempDao.getAccounts();
    }

    public Observable<ReactiveResult<Tuple>> getLabels() {
        return tempDao.getLabels();
    }

    public interface RestoreHandler {
        void addAccount(BackupAccount backupAccount) throws Exception;

        void addLabel(BackupLabel backupLabel) throws Exception;
    }
}
