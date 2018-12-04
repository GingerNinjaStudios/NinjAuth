package me.gingerninja.authenticator.util.backup;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.google.gson.Gson;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.data.pojo.BackupFile;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import timber.log.Timber;

public class Restore {
    private final Context context;
    private final AccountRepository accountRepo;
    private final Gson gson;
    private final Uri uri;
    private final File tmpFile;

    public Restore(Context context, AccountRepository accountRepo, Gson gson, @NonNull Uri uri) {
        this.context = context;
        this.accountRepo = accountRepo;
        this.gson = gson;
        this.uri = uri;

        tmpFile = new File(context.getCacheDir(), "tmp_backup.zip");
    }

    private void deletePrevious() {
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
    }

    private void transferZipFile() throws IOException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
        FileInputStream fis = new FileInputStream(pfd.getFileDescriptor());
        FileOutputStream fos = new FileOutputStream(tmpFile);

        byte[] buffer = new byte[1024];
        int read;

        while ((read = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }

        fos.flush();
        fos.close();
        pfd.close();
        fis.close();
    }

    public Completable restore(@Nullable final char[] password) {
        return Completable
                .create(emitter -> {
                    try {
                        internalRestore(password);
                        emitter.onComplete();
                    } catch (Throwable t) {
                        emitter.tryOnError(t);
                    } finally {
                        if (tmpFile.exists()) {
                            tmpFile.delete();
                        }
                    }
                })
                .observeOn(Schedulers.io());
    }

    private void internalRestore(@Nullable char[] password) throws IOException, ZipException {
        deletePrevious();
        transferZipFile();

        ZipFile zipFile = new ZipFile(tmpFile);
        if (zipFile.isEncrypted()) {
            zipFile.setPassword(password);
        }

        FileHeader dataFileHeader = zipFile.getFileHeader(BackupUtils.DATA_FILE_NAME);
        if (dataFileHeader == null) {
            // this is not an appropriate file
            throw new IllegalArgumentException();
        } else {
            Reader in = new InputStreamReader(zipFile.getInputStream(dataFileHeader), "UTF-8");
            BackupFile backupFile = gson.fromJson(in, BackupFile.class);

            Timber.d("BackupFile - accounts: %s, labels: %s", backupFile.getAccounts(), backupFile.getLabels());
            // TODO process backup file
        }
    }
}
