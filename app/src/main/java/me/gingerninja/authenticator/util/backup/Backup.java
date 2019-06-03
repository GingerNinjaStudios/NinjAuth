package me.gingerninja.authenticator.util.backup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.pojo.BackupAccount;
import me.gingerninja.authenticator.data.pojo.BackupFile;
import me.gingerninja.authenticator.data.pojo.BackupLabel;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.Parser;

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

    private void addData(List<Account> accountList, ZipFile zipFile, ZipParameters zipParameters) throws IOException, ZipException {
        // add data file to the ZIP
        BackupFile backupFile = new BackupFile();

        List<BackupAccount> backupAccounts = Observable
                .fromIterable(accountList)
                .map(BackupAccount::fromEntity)
                .toList()
                .blockingGet();

        List<BackupLabel> backupLabels = accountRepo
                .getAllLabel()
                .map(BackupLabel::fromEntity)
                .toList()
                .blockingGet();

        backupFile.setAccounts(backupAccounts);
        backupFile.setLabels(backupLabels);

        ByteArrayOutputStream dataBos = new ByteArrayOutputStream();
        OutputStreamWriter dataOSW = new OutputStreamWriter(dataBos, StandardCharsets.UTF_8);
        gson.toJson(backupFile, dataOSW);
        dataOSW.flush();
        dataOSW.close();

        ByteArrayInputStream dataBis = new ByteArrayInputStream(dataBos.toByteArray());
        zipParameters.setSourceExternalStream(true);
        zipParameters.setFileNameInZip(BackupUtils.DATA_FILE_NAME);
        zipFile.addStream(dataBis, zipParameters);

        dataBos.close();
        dataBis.close();
    }

    private void addQrImages(List<Account> accountList, ZipFile zipFile, ZipParameters zipParameters) {
        // add QR code images to the ZIP
        Observable.fromIterable(accountList)
                .blockingSubscribe(account -> {
                    String url = Parser.createUrl(account);
                    InputStream is = bitmapToInputStream(createQrCode(url));
                    //ZipParameters params = new ZipParameters();
                    zipParameters.setSourceExternalStream(true);
                    zipParameters.setFileNameInZip(account.getPosition() + "-" + URLEncoder.encode(account.getAccountName(), "UTF-8") + ".png");
                    zipFile.addStream(is, zipParameters);

                    is.close();
                });
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

    public Completable export(@Nullable final char[] password) {
        return Completable
                .create(emitter -> {
                    try {
                        internalExport(password);
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

    private void internalExport(@Nullable char[] password) throws ZipException, IOException {
        List<Account> accountList = accountRepo.getAllAccountAndListen().blockingFirst(Collections.emptyList());

        deletePrevious();

        ZipFile zipFile = new ZipFile(tmpFile);
        // Setting parameters
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

        if (password != null && password.length > 0) {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(password);
        }

        addData(accountList, zipFile, zipParameters);
        addQrImages(accountList, zipFile, zipParameters);
        transferZipFile(zipFile);
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

        /*int[] pixels = new int[actualWidth * actualHeight];

        for (int y = 0; y < actualHeight; y++) {
            int offset = y * actualWidth;

            for (int x = 0; x < actualWidth; x++) {
                pixels[offset + x] = encoded.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        bitmap.setPixels(pixels, 0, actualWidth, 0, 0, actualWidth, actualHeight);*/
        for (int i = 0; i < actualWidth; i++) {
            for (int j = 0; j < actualHeight; j++) {
                bitmap.setPixel(i, j, encoded.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }

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
}
