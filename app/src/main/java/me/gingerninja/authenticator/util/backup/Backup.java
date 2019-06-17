package me.gingerninja.authenticator.util.backup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.pojo.BackupAccount;
import me.gingerninja.authenticator.data.pojo.BackupLabel;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.Parser;
import timber.log.Timber;

public class Backup {
    @NonNull
    private final Context context;

    @NonNull
    private final Uri uri;

    @NonNull
    private final AccountRepository accountRepo;

    @NonNull
    private final Gson gson;

    @NonNull
    private final File tmpFile;

    public Backup(Context context, AccountRepository accountRepo, Gson gson, @NonNull Uri uri) {
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

        /*DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null) {
            documentFile.delete(); // TODO check if it's true
        }*/
    }

    private void transferZipFile(ZipFile zipFile) throws IOException {
        ParcelFileDescriptor outputFd = context.getContentResolver().openFileDescriptor(uri, "w");
        FileOutputStream fos = new FileOutputStream(outputFd.getFileDescriptor());//openFileForWrite(uri);
        FileInputStream fis = new FileInputStream(zipFile.getFile());

        byte[] buffer = new byte[1024];
        int read;

        while ((read = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }

        fos.flush();
        fos.close();
        outputFd.close();
        fis.close();
    }

    public Completable export(@NonNull final Options options) {
        return Completable
                .create(emitter -> {
                    try {
                        internalExport(options);
                        emitter.onComplete();
                    } catch (Throwable t) {
                        emitter.tryOnError(t);
                    } finally {
                        if (tmpFile.exists()) {
                            tmpFile.delete();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    private void internalExport(@NonNull final Options options) throws ZipException, IOException {
        deletePrevious();

        ZipFile zipFile = new ZipFile(tmpFile);
        // Setting parameters
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

        if (options.password != null && options.password.length > 0) {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(options.password);
        }

        zipParameters.isSourceExternalStream();

        Observable<Account> accountObservable = accountRepo.getAccounts(); // TODO filter accounts
        Observable<Label> labelObservable = accountRepo.getAllLabel(); // TODO filter labels

        addData(accountObservable, labelObservable, options, zipFile, zipParameters).blockingAwait();

        if (options.withAccountImages()) {
            addImages(accountObservable, zipFile, zipParameters);
        }

        Timber.v("ZIP file complete, transfering");

        transferZipFile(zipFile);
    }

    private Completable addData(Observable<Account> accounts, Observable<Label> labels, @NonNull final Options options, ZipFile zipFile, ZipParameters zipParameters) throws IOException {
        //ByteArrayOutputStream dataBos = new ByteArrayOutputStream();
        PipedOutputStream dataBos = new PipedOutputStream();
        OutputStreamWriter dataOSW = new OutputStreamWriter(dataBos, StandardCharsets.UTF_8);

        JsonWriter writer = gson.newJsonWriter(dataOSW);

        //ByteArrayInputStream dataBis = new ByteArrayInputStream(dataBos.toByteArray());
        PipedInputStream dataBis = new PipedInputStream(dataBos, 8096);
        zipParameters.setSourceExternalStream(true);
        zipParameters.setFileNameInZip(BackupUtils.DATA_FILE_NAME);
        // zipFile.addStream(dataBis, zipParameters);

        Completable zipWork = Completable
                .create(emitter -> {
                    Timber.v("ZIP thread: %s", Thread.currentThread());
                    zipFile.addStream(dataBis, zipParameters);
                    dataBis.close();
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.newThread());

        Completable accountsWork = accounts
                .doOnSubscribe(disposable -> {
                    writer.name("accounts");
                    writer.beginArray();
                })
                .doOnComplete(writer::endArray)
                .collectInto(writer, (jsonWriter, account) -> {
                    BackupAccount backupAccount = BackupAccount.fromEntity(account);
                    gson.toJson(backupAccount, BackupAccount.class, jsonWriter);
                })
                .ignoreElement();

        Completable labelsWork = labels
                .doOnSubscribe(disposable -> {
                    writer.name("labels");
                    writer.beginArray();
                })
                .doOnComplete(writer::endArray)
                .collectInto(writer, (jsonWriter, account) -> {
                    BackupLabel backupLabel = BackupLabel.fromEntity(account);
                    gson.toJson(backupLabel, BackupLabel.class, jsonWriter);
                })
                .ignoreElement();

        /*dataOSW.flush();
        dataBis.close();

        dataOSW.close();
        dataBos.close();*/

        Completable dataWork = Completable.concatArray(accountsWork, labelsWork)
                .doOnSubscribe(disposable -> {
                    Timber.v("Data thread SUB: %s", Thread.currentThread());
                    writer.beginObject();

                    BackupMeta meta = new BackupMeta.Builder()
                            .setAutoBackup(options.isAutoBackup)
                            .setComment(options.comment)
                            .build();

                    writer.name("meta");
                    gson.toJson(meta, BackupMeta.class, writer);

                    writer.name("data");
                    writer.beginObject();
                })
                .doOnTerminate(() -> {
                    Timber.v("Data thread TER: %s", Thread.currentThread());
                    writer.endObject(); // data
                    writer.endObject(); // document

                    dataOSW.flush();
                    //dataBis.close();

                    dataOSW.close();
                    dataBos.close();
                });

        return Completable.mergeArray(dataWork, zipWork);
    }

    private void addImages(Observable<Account> accountObservable, ZipFile zipFile, ZipParameters params) {
        accountObservable
                .blockingSubscribe(account -> {
                    String url = Parser.createUrl(account);
                    Bitmap bitmap = createQrCode(url);
                    InputStream is = bitmapToInputStream(bitmap);
                    ZipParameters zipParameters = (ZipParameters) params.clone();
                    zipParameters.setSourceExternalStream(true);
                    zipParameters.setFileNameInZip(account.getPosition() + "-" + URLEncoder.encode(account.getAccountName(), "UTF-8") + ".png");
                    zipFile.addStream(is, zipParameters);

                    is.close();
                });
    }

    @Nullable
    private Bitmap createQrCode(String value) throws WriterException {
        BitMatrix encoded;
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            encoded = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 512, 512, hints);

        } catch (IllegalArgumentException e) {
            return null;
        }
        int actualWidth = encoded.getWidth();
        int actualHeight = encoded.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_4444);
        int[] pixels = new int[actualWidth * actualHeight];

        for (int y = 0, offset = 0; y < actualHeight; y++, offset += actualWidth) {
            for (int x = 0; x < actualWidth; x++) {
                pixels[offset + x] = encoded.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        bitmap.setPixels(pixels, 0, actualWidth, 0, 0, actualWidth, actualHeight);

        /*for (int i = 0; i < actualWidth; i++) {
            for (int j = 0; j < actualHeight; j++) {
                bitmap.setPixel(i, j, encoded.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }*/

        return bitmap;
    }

    @NonNull
    private InputStream bitmapToInputStream(@NonNull Bitmap bitmap) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        bitmap.recycle();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        baos.close();

        return bais;
    }

    public static class Options {
        final char[] password;
        private boolean withAccountImages;
        private String comment;
        private boolean isAutoBackup;

        private Options(@Nullable char[] password) {
            this.password = password;
        }

        public boolean withAccountImages() {
            return withAccountImages;
        }

        public String getComment() {
            return comment;
        }

        public boolean isAutoBackup() {
            return isAutoBackup;
        }

        public static class Builder {
            private char[] password;
            private boolean withAccountImages;
            private String comment;
            private boolean isAutoBackup;

            public Builder() {
            }

            public Builder password(@Nullable char[] password) {
                this.password = password;
                return this;
            }

            public Builder withAccountImages(boolean withAccountImages) {
                this.withAccountImages = withAccountImages;
                return this;
            }

            public Builder setComment(String comment) {
                this.comment = comment;
                return this;
            }

            public Builder setAutoBackup(boolean autoBackup) {
                isAutoBackup = autoBackup;
                return this;
            }

            public Options build() {
                Options options = new Options(password);
                options.withAccountImages = withAccountImages;
                options.comment = comment;
                options.isAutoBackup = isAutoBackup;
                return options;
            }
        }
    }
}
