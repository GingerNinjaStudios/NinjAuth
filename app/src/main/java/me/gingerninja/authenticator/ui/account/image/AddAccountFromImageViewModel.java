package me.gingerninja.authenticator.ui.account.image;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.InputStream;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.util.Parser;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public class AddAccountFromImageViewModel extends ViewModel {
    static final String RESULT_OK = "account.image.ok";
    static final String RESULT_CANCEL = "account.image.cancel";
    static final String RESULT_ERROR = "account.image.error";
    static final String RESULT_BROWSE = "account.image.browse";

    @NonNull
    private Context context;

    private BarcodeDetector detector;

    private CompositeDisposable disposable = new CompositeDisposable();

    private MutableLiveData<SingleEvent<String>> results = new MutableLiveData<>();

    public ObservableBoolean processing = new ObservableBoolean(true);
    public ObservableBoolean success = new ObservableBoolean(false);
    public ObservableInt errorMsg = new ObservableInt(R.string.account_from_image_error_unknown_error);

    @Inject
    public AddAccountFromImageViewModel(@NonNull Context context) {
        this.context = context.getApplicationContext();
        detector = new BarcodeDetector.Builder(this.context).setBarcodeFormats(Barcode.QR_CODE).build();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (detector != null) {
            detector.release();
        }

        disposable.clear();
    }

    public void onCancelClick(@Nullable View v) {
        disposable.clear();
        results.setValue(new SingleEvent<>(RESULT_CANCEL));
    }

    public void onBrowseClick(View v) {
        results.setValue(new SingleEvent<>(RESULT_BROWSE));
    }

    void processResults(@NonNull Intent data) {
        disposable.clear();
        errorMsg.set(R.string.account_from_image_error_unknown_error);
        processing.set(true);
        success.set(false);

        disposable.add(
                decodeBarcode(data).subscribe(this::handleDetection, this::handleError)
        );
    }

    private void handleDetection(@NonNull String data) {
        processing.set(false);
        success.set(true);
        results.setValue(new SingleEvent<>(RESULT_OK, data));
    }

    private void handleError(Throwable throwable) {
        Timber.e(throwable, "Barcode error: %s", throwable.getMessage());

        if (throwable instanceof BarcodeException) {
            int errorCode = ((BarcodeException) throwable).getError();
            switch (errorCode) {
                case BarcodeError.INVALID_IMAGE:
                    errorMsg.set(R.string.account_from_image_error_invalid_image);
                    break;
                case BarcodeError.NOT_OTP_BARCODE:
                    errorMsg.set(R.string.account_from_image_error_not_otp);
                    break;
                case BarcodeError.NO_BARCODES_FOUND:
                    errorMsg.set(R.string.account_from_image_error_no_barcode);
                    break;
                case BarcodeError.TOO_MANY_BARCODES:
                    errorMsg.set(R.string.account_from_image_error_too_many_barcode);
                    break;
            }
        } else if (throwable instanceof IllegalArgumentException) {
            errorMsg.set(R.string.account_from_image_error_invalid_image);
        }

        processing.set(false);
        success.set(false);

        results.setValue(new SingleEvent<>(RESULT_ERROR, throwable));
    }

    LiveData<SingleEvent<String>> getResults() {
        return results;
    }

    @NonNull
    private Single<String> decodeBarcode(@NonNull Intent data) {
        Uri uri = data.getData();

        if (uri == null) {
            return Single.error(new BarcodeException(BarcodeError.NO_BARCODES_FOUND));
        }

        return Single
                .fromCallable(() -> {
                    try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        if (bitmap == null) {
                            throw new BarcodeException(BarcodeError.INVALID_IMAGE);
                        }
                        return bitmap;
                    }
                })
                .map(bitmap -> new Frame.Builder().setBitmap(bitmap).build())
                .map(frame -> {
                    SparseArray<Barcode> barcodes = detector.detect(frame);
                    int n = barcodes.size();
                    if (n == 0) {
                        throw new BarcodeException(BarcodeError.NO_BARCODES_FOUND);
                    } else if (n > 1) {
                        throw new BarcodeException(BarcodeError.TOO_MANY_BARCODES);
                    } else {
                        Barcode barcode = barcodes.valueAt(0);
                        String url = barcode.rawValue;

                        Account account = Parser.parseUrl(url);

                        if (account != null) {
                            return url;
                        } else {
                            throw new BarcodeException(BarcodeError.NOT_OTP_BARCODE);
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @IntDef({BarcodeError.NO_BARCODES_FOUND, BarcodeError.TOO_MANY_BARCODES, BarcodeError.NOT_OTP_BARCODE, BarcodeError.INVALID_IMAGE})
    @interface BarcodeError {
        int NO_BARCODES_FOUND = 0;
        int TOO_MANY_BARCODES = 1;
        int NOT_OTP_BARCODE = 2;
        int INVALID_IMAGE = 3;
    }

    public static class BarcodeException extends RuntimeException {
        @BarcodeError
        private final int error;

        private BarcodeException(@BarcodeError int error) {
            this.error = error;
        }

        @BarcodeError
        public int getError() {
            return error;
        }
    }
}
