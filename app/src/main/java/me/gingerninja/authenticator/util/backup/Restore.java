package me.gingerninja.authenticator.util.backup;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.exception.ZipExceptionConstants;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.data.pojo.BackupAccount;
import me.gingerninja.authenticator.data.pojo.BackupFile;
import me.gingerninja.authenticator.data.pojo.BackupLabel;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import timber.log.Timber;

public class Restore {
    private final Context context;
    private final TemporaryRepository repo;
    private final Gson gson;
    private final Uri uri;
    private final File tmpFile;

    private ZipFile zipFile;

    public Restore(Context context, TemporaryRepository repo, Gson gson, @NonNull Uri uri) {
        this.context = context;
        this.repo = repo;
        this.gson = gson;
        this.uri = uri;

        tmpFile = new File(context.getCacheDir(), "tmp_backup.zip");
    }

    public String getDisplayName() {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String name = cursor.getString(nameIndex);
        cursor.close();
        return name;
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

    public Single<Restore> prepare() {
        return Single.
                <Restore>create(emitter -> {
                    try {
                        deletePrevious();
                        transferZipFile();
                        zipFile = new ZipFile(tmpFile);
                        if (zipFile.isValidZipFile()) {
                            emitter.onSuccess(Restore.this);
                        } else {
                            emitter.tryOnError(new ZipException("Not a ZIP file", ZipExceptionConstants.notZipFile));
                        }
                    } catch (Throwable t) {
                        emitter.tryOnError(t);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    private void checkZipFile() {
        if (zipFile == null) {
            throw new IllegalStateException("Call prepare() before doing any work");
        }
    }

    public boolean isPasswordNeeded() throws ZipException {
        checkZipFile();

        return zipFile.isEncrypted();
    }

    public Completable readDataFile(@Nullable final char[] password) {
        checkZipFile();

        return Completable
                .<BackupFile>create(emitter -> {
                    try {
                        internalRestore(password).blockingAwait();
                        emitter.onComplete();
                        dispose();
                    } catch (Throwable t) {
                        if (!emitter.tryOnError(t)) {
                            dispose();
                        } else {
                            if (t instanceof ZipException && ((ZipException) t).getCode() != ZipExceptionConstants.WRONG_PASSWORD) {
                                dispose();
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public Completable restore() {
        return repo.restoreAndPurge();
    }

    @WorkerThread
    private Completable internalRestore(@Nullable char[] password) throws ZipException {
        if (zipFile.isEncrypted()) {
            if (password == null || password.length == 0) {
                throw new ZipException("", ZipExceptionConstants.WRONG_PASSWORD);
            }

            zipFile.setPassword(password);
        }

        FileHeader dataFileHeader = zipFile.getFileHeader(BackupUtils.DATA_FILE_NAME);
        if (dataFileHeader == null) {
            // this is not an appropriate file
            throw new NotNinjAuthZipFile();
        } else {
            //try (Reader in = new InputStreamReader(zipFile.getInputStream(dataFileHeader), StandardCharsets.UTF_8)) {
            Reader in = new InputStreamReader(zipFile.getInputStream(dataFileHeader), StandardCharsets.UTF_8);
            return repo.preprocessRestore(new DatabaseProcessorImpl(in));

            //readData(in);
            //return gson.fromJson(in, BackupFile.class);
            //return new BackupFile();
            //} catch (IOException e) {
            //    throw new ZipException(e, ZipExceptionConstants.randomAccessFileNull);
            //}
        }
    }

    /*private void readData(@NonNull Reader in) throws IOException {
        JsonReader jsonReader = new JsonReader(in);

        JsonToken token;

        while ((token = jsonReader.peek()) != JsonToken.END_DOCUMENT) {
            switch (token) {
                case NAME:
                    String name = jsonReader.nextName();
                    switch (name) {
                        case "accounts":
                            readAccounts(jsonReader);
                            break;
                        case "labels":
                            readLabels(jsonReader);
                            break;
                        default:
                            jsonReader.skipValue();
                    }
                    break;
                case BEGIN_OBJECT:
                    jsonReader.beginObject();
                    break;
                case END_OBJECT:
                    jsonReader.endObject();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }

        jsonReader.close();

        Timber.v("END of JSON");
    }

    private void readAccounts(@NonNull JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();

        if (token != JsonToken.BEGIN_ARRAY) {
            return;
        }

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            BackupAccount backupFile = gson.fromJson(jsonReader, BackupAccount.class);
            Timber.v("Read account: %s, labels: %s", backupFile.toEntity(), Arrays.toString(backupFile.getLabelIds()));
            // TO DO save account
        }
        jsonReader.endArray();
    }

    private void readLabels(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();

        if (token != JsonToken.BEGIN_ARRAY) {
            return;
        }

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            BackupLabel backup = gson.fromJson(jsonReader, BackupLabel.class);
            Timber.v("Read label: %s", backup.toEntity());
            // TO DO save label
        }
        jsonReader.endArray();
    }*/

    public void dispose() {
        zipFile = null;

        if (tmpFile.exists()) {
            tmpFile.delete();
        }
    }

    public interface DatabaseProcessor {

        void process(@NonNull TemporaryRepository.RestoreHandler restoreHandler) throws Exception;
    }

    public class DatabaseProcessorImpl implements DatabaseProcessor {
        private final JsonReader jsonReader;

        private DatabaseProcessorImpl(@NonNull Reader in) {
            jsonReader = new JsonReader(in);
        }

        @Override
        public void process(@NonNull TemporaryRepository.RestoreHandler restoreHandler) throws Exception {
            JsonToken token;

            while ((token = jsonReader.peek()) != JsonToken.END_DOCUMENT) {
                switch (token) {
                    case NAME:
                        String name = jsonReader.nextName();
                        switch (name) {
                            case "accounts":
                                readAccounts(restoreHandler, jsonReader);
                                break;
                            case "labels":
                                readLabels(restoreHandler, jsonReader);
                                break;
                            default:
                                jsonReader.skipValue();
                        }
                        break;
                    case BEGIN_OBJECT:
                        jsonReader.beginObject();
                        break;
                    case END_OBJECT:
                        jsonReader.endObject();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.close();

            Timber.v("END of JSON");
        }

        private void readAccounts(@NonNull TemporaryRepository.RestoreHandler restoreHandler, @NonNull JsonReader jsonReader) throws Exception {
            JsonToken token = jsonReader.peek();

            if (token != JsonToken.BEGIN_ARRAY) {
                return;
            }

            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                BackupAccount account = gson.fromJson(jsonReader, BackupAccount.class);
                restoreHandler.addAccount(account);
            }
            jsonReader.endArray();
        }

        private void readLabels(@NonNull TemporaryRepository.RestoreHandler restoreHandler, JsonReader jsonReader) throws Exception {
            JsonToken token = jsonReader.peek();

            if (token != JsonToken.BEGIN_ARRAY) {
                return;
            }

            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                BackupLabel label = gson.fromJson(jsonReader, BackupLabel.class);
                restoreHandler.addLabel(label);
            }
            jsonReader.endArray();
        }
    }
}
