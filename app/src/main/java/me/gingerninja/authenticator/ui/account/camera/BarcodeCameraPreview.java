package me.gingerninja.authenticator.ui.account.camera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BarcodeCameraPreview extends SurfaceView implements SurfaceHolder.Callback, Detector.Processor<Barcode> {
    private static final String TAG = BarcodeCameraPreview.class.getSimpleName();
    private final BarcodeDetector barcodeDetector;
    private boolean startRequested;
    private boolean isPlaying;
    private boolean surfaceReady;
    private Detector.Processor<Barcode> barcodeProcessor;
    private CameraSource cameraSource;

    private Disposable optimalSizeDisposable;

    {
        barcodeDetector = new BarcodeDetector.Builder(getContext()).setBarcodeFormats(Barcode.QR_CODE).build();
        barcodeDetector.setProcessor(this);
        getHolder().addCallback(this);
    }

    public BarcodeCameraPreview(Context context) {
        super(context);
    }

    public BarcodeCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarcodeCameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BarcodeCameraPreview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    public static Camera.Size getOptimalPreviewSize(@Nullable List<Camera.Size> sizes, int w, int h) {
        Log.d(TAG, "getOptimalPreviewSize()");
        if (sizes != null) {
            for (Camera.Size size : sizes) {
                Log.d(TAG, "-- size: " + size.width + "x" + size.height);
            }
        }
        /*if (w > h) {
            int tmp = h;
            h = w;
            w = tmp;
        }*/

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;
        //double targetRatio = h > w ? (double) h / w : (double) w / h;

        Log.d(TAG, "Target ratio: " + targetRatio);

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            //Log.d("CameraPreview", "size " + size.width + "x" + size.height);

            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public boolean isDetectorOperational() {
        return barcodeDetector.isOperational();
    }

    public void start() {
        if (isPlaying) {
            return;
        }

        startRequested = true;
        internalStart();
    }

    @SuppressLint("MissingPermission")
    private void internalStart() {
        Log.d("BarcodeCameraPreview", "internalStart()");
        if (isPlaying) {
            return;
        }

        if (startRequested && surfaceReady && cameraSource != null) {
            try {
                Log.d("BarcodeCameraPreview", "Starting CameraSource");
                cameraSource.start(getHolder());
                isPlaying = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (cameraSource != null && isPlaying) {
            cameraSource.stop();
            Log.d("BarcodeCameraPreview", "Stopping CameraSource");
        }

        startRequested = false;
        isPlaying = false;
    }

    public void releaseCamera() {
        stop();

        if (barcodeDetector != null) {
            barcodeDetector.release();
        }

        if (cameraSource != null) {
            cameraSource.release();
            Log.d("BarcodeCameraPreview", "Releasing CameraSource");
            cameraSource = null;
        }
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int h = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        Log.d(TAG, "onMeasure(): " + w + "x" + h);

        setMeasuredDimension(w, h);
    }*/

    public void setBarcodeProcessor(Detector.Processor<Barcode> processor) {
        this.barcodeProcessor = processor;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //getHolder().setFixedSize(1024, 1024);
        surfaceReady = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
        release();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (optimalSizeDisposable != null && !optimalSizeDisposable.isDisposed()) {
            optimalSizeDisposable.dispose();
        }

        release();

        optimalSizeDisposable = findOptimalSize(width, height, (w, h) -> {
            CameraSource.Builder cameraSourceBuilder = new CameraSource.Builder(getContext(), barcodeDetector)
                    .setAutoFocusEnabled(true)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(15f);

            Log.d(TAG, "Optimal size: " + w + "x" + h);

            if (w > -1 && h > -1) {
                cameraSourceBuilder.setRequestedPreviewSize(w, h);
            }

            cameraSource = cameraSourceBuilder.build();

            internalStart();
        });
    }

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        if (barcodeProcessor != null) {
            barcodeProcessor.receiveDetections(detections);
        }
    }

    @Override
    public void release() {
        if (barcodeProcessor != null) {
            barcodeProcessor.release();
        }
    }

    public Disposable findOptimalSize(int w, int h, OptimalSizeCallback callback) {
        Log.d(TAG, "Finding optimal size for " + w + "x" + h);
        return Single
                .<Integer>create(emitter -> {
                    final int n = Camera.getNumberOfCameras();
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    for (int i = 0; i < n; i++) {
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            emitter.onSuccess(i);
                            break;
                        }
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .map(cameraId -> {
                    Camera camera = Camera.open(cameraId);
                    List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
                    camera.release();

                    Camera.Size optimalSize = getOptimalPreviewSize(previewSizes, w, h);

                    Pair<Integer, Integer> retVal;
                    if (optimalSize == null) {
                        retVal = new Pair<>(-1, -1);
                    } else {
                        retVal = new Pair<>(optimalSize.width, optimalSize.height);
                    }

                    return retVal;
                })
                .subscribe(integerCameraInfoPair -> callback.optimalSizeFound(integerCameraInfoPair.first, integerCameraInfoPair.second));
    }

    private interface OptimalSizeCallback {
        void optimalSizeFound(int w, int h);
    }
}
