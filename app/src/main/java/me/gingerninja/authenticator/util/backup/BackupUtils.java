package me.gingerninja.authenticator.util.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.Completable;
import io.reactivex.annotations.CheckReturnValue;
import me.gingerninja.authenticator.data.repo.AccountRepository;

@Singleton
public class BackupUtils {
    static final String DATA_FILE_NAME = "_data.json";

    private final Context context;
    private final AccountRepository accountRepo;
    private final Gson gson;

    @Inject
    public BackupUtils(Context context, AccountRepository accountRepo, Gson gson) {
        this.context = context;
        this.accountRepo = accountRepo;
        this.gson = gson;
    }

    @CheckReturnValue
    public Completable backup(@NonNull Uri uri) {
        char[] password = "alma".toCharArray(); // TODO

        return new Backup(context, accountRepo, gson, uri).export(password);
    }

    @CheckReturnValue
    public Restore restore(@NonNull Uri uri) {
        return new Restore(context, accountRepo, gson, uri);
    }

    /*public void backup(@NonNull Uri uri) throws ZipException, IOException {
        File tmpFile = new File(context.getCacheDir(), "tmp_backup.zip");
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null) {
            documentFile.delete(); // TODO check if it's true
        }

        ZipFile zipFile = new ZipFile(tmpFile);
        // Setting parameters
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_128);
        // Setting password
        zipParameters.setPassword("pass@123");

        List<Account> accountList = accountRepo.getAllAccountAndListen().blockingFirst(Collections.emptyList());

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
        zipParameters.setFileNameInZip(DATA_FILE_NAME);
        zipFile.addStream(dataBis, zipParameters);

        dataBos.close();
        dataBis.close();

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

        ParcelFileDescriptor outputFd = context.getContentResolver().openFileDescriptor(uri, "w");
        FileOutputStream fos = new FileOutputStream(outputFd.getFileDescriptor());//openFileForWrite(uri);
        FileInputStream fis = new FileInputStream(zipFile.getFile());

        byte[] buffer = new byte[1024];
        int read;

        while ((read = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }

        fos.flush();
        closeFile(fos);
        outputFd.close();
        fis.close();

        if (tmpFile.exists()) {
            tmpFile.delete();
        }
    }*/

    /*public void restore() throws ZipException, IOException {
        File tmpFile = new File(context.getCacheDir(), "tmp_backup.zip");
        ZipFile zipFile = new ZipFile(tmpFile);
        // If it is encrypted then provide password
        if (zipFile.isEncrypted()) {
            zipFile.setPassword("pass@123");
        }
        //zipFile.extractAll(destPath);

        FileHeader dataFileHeader = zipFile.getFileHeader(DATA_FILE_NAME);
        if (dataFileHeader == null) {
            // TODO this is not an appropriate file
        } else {
            Reader in = new InputStreamReader(zipFile.getInputStream(dataFileHeader), "UTF-8");
            BackupFile backupFile = gson.fromJson(in, BackupFile.class);

            // TODO process backup file
        }
    }*/

    public void createFile(@NonNull Activity activity, int requestCode, @NonNull String fileName, boolean persistable) {
        Intent intent = getCreateFileIntent(fileName, persistable);
        activity.startActivityForResult(intent, requestCode);
    }

    public void createFile(@NonNull Fragment fragment, int requestCode, @NonNull String fileName, boolean persistable) {
        Intent intent = getCreateFileIntent(fileName, persistable);
        fragment.startActivityForResult(intent, requestCode);
    }

    public void openFile(@NonNull Activity activity, int requestCode, boolean persistable) {
        Intent intent = getOpenFileIntent(persistable);
        activity.startActivityForResult(intent, requestCode);
    }

    public void openFile(@NonNull Fragment fragment, int requestCode, boolean persistable) {
        Intent intent = getOpenFileIntent(persistable);
        fragment.startActivityForResult(intent, requestCode);
    }

    @NonNull
    private FileOutputStream openFileForWrite(Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
        assert pfd != null;
        return new FileOutputStream(pfd.getFileDescriptor());
    }

    @NonNull
    private FileInputStream openFileForRead(Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "rw");
        assert pfd != null;
        return new FileInputStream(pfd.getFileDescriptor());
    }

    /*private void openFileForWrite(Uri uri) {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "rw");
            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();

                BufferedReader reader = new BufferedReader(new FileReader(fd));

                // TODO reader

                reader.close();

                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fd));

                // TODO fileWriter

                closeFile(fileWriter);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private void closeFile(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Intent getCreateFileIntent(@NonNull String fileName, boolean persistable) {
        String mimeType = "application/zip"; // TODO or application/octet-stream
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType(mimeType);

        if (persistable) {
            intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }

        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        return intent;
    }

    private static Intent getOpenFileIntent(boolean persistable) {
        String mimeType = "application/zip"; // TODO or application/octet-stream
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType(mimeType);

        if (persistable) {
            intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }

        return intent;
    }

    @Nullable
    public Uri getUriFromIntent(@Nullable Intent data) {
        Uri uri = null;
        if (data != null) {
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            uri = data.getData();
            if (uri != null && takeFlags != 0) {
                context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
            }
        }

        return uri;
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
