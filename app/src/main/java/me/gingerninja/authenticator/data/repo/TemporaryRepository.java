package me.gingerninja.authenticator.data.repo;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import me.gingerninja.authenticator.data.db.dao.TempDao;
import me.gingerninja.authenticator.data.pojo.BackupAccount;
import me.gingerninja.authenticator.data.pojo.BackupLabel;
import me.gingerninja.authenticator.util.backup.Restore;

@Singleton
public class TemporaryRepository {
    private final TempDao tempDao;

    @Inject
    public TemporaryRepository(TempDao tempDao) {
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

    public interface RestoreHandler {
        void addAccount(BackupAccount backupAccount) throws Exception;

        void addLabel(BackupLabel backupLabel) throws Exception;
    }
}
