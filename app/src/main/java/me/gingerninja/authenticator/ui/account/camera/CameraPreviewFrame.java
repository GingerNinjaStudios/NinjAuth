package me.gingerninja.authenticator.ui.account.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;
import androidx.core.util.Pair;
import androidx.core.view.MarginLayoutParamsCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@SuppressLint("LogNotTimber")
public class CameraPreviewFrame extends FrameLayout implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreviewFrame";
    private SurfaceView cameraSurfaceView;
    private boolean startRequested;
    private boolean isPlaying;
    private boolean surfaceReady;
    private boolean cameraPreviewAttached;
    private BarcodeDetector barcodeDetector;
    private Detector.Processor<Barcode> barcodeProcessor;
    private final Detector.Processor<Barcode> internalProcessor = new Detector.Processor<Barcode>() {
        @Override
        public void release() {
            if (barcodeProcessor != null) {
                barcodeProcessor.release();
            }
        }

        @Override
        public void receiveDetections(Detector.Detections<Barcode> detections) {
            if (barcodeProcessor != null) {
                int w = getMeasuredWidth();
                int h = getMeasuredHeight();
                float ratio = w / (float) h;

                Frame.Metadata meta = detections.getFrameMetadata();
                int metaW = meta.getWidth();
                int metaH = meta.getHeight();
                float metaRatio = metaW / (float) metaH;

                float mutualRatio = ratio / metaRatio;

                Rect cameraRect;
                if (mutualRatio > 1f) {
                    // width is 1.0
                    // height is dynamic
                    cameraRect = new Rect(0, 0, meta.getWidth(), (int) (meta.getHeight() / mutualRatio));
                } else {
                    cameraRect = new Rect(0, 0, (int) (meta.getWidth() * mutualRatio), meta.getHeight());
                }

                cameraRect.offset((metaW - cameraRect.width()) / 2, (metaH - cameraRect.height()) / 2);
                cameraRect.inset(-(cameraRect.width() / 100), -(cameraRect.height() / 100));

                //Timber.d("View dimensions: %dx%d, frame dims: %dx%d", getMeasuredWidth(), getMeasuredHeight(), meta.getWidth(), meta.getHeight());
                // Timber.d("Camera rect: %s", cameraRect.toShortString());

                SparseArray<Barcode> detectedItems = detections.getDetectedItems();
                int[] ignoredKeys = new int[detectedItems.size()];
                int ignoredKeysIndex = 0;

                for (int i = 0; i < detectedItems.size(); i++) {
                    Barcode barcode = detectedItems.valueAt(i);
                    if (!cameraRect.contains(barcode.getBoundingBox())) {
                        ignoredKeys[ignoredKeysIndex++] = detectedItems.keyAt(i);
                    }
                    //Timber.d("Barcode bounding box: %s, is inside the camera view: %s", barcode.getBoundingBox().toShortString(), cameraRect.contains(barcode.getBoundingBox()));
                    //2018-09-25 17:28:06.006 32216-32472/me.gingerninja.authenticator D/CameraPreviewFrame: View dimensions: 1080x1080, frame dims: 960x1280
                    //2018-09-25 17:28:06.006 32216-32472/me.gingerninja.authenticator D/CameraPreviewFrame: Barcode bounding box: [382,59][628,373]
                }

                for (int i = 0; i < ignoredKeysIndex; i++) {
                    detectedItems.remove(ignoredKeys[i]);
                }

                barcodeProcessor.receiveDetections(detections);
            }
        }
    };
    private CameraSource cameraSource;
    private int[] selectedPreviewSize = new int[]{-1, -1};

    {
        cameraSurfaceView = new SurfaceView(getContext());
        cameraSurfaceView.getHolder().addCallback(this);

        //attachCameraView(1080, 1080);
    }

    public CameraPreviewFrame(@NonNull Context context) {
        super(context);
    }

    public CameraPreviewFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreviewFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraPreviewFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "onMeasure(): " + w + "x" + h);

        if (!cameraPreviewAttached && PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.CAMERA)) {
            attachCameraView(w, h);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }*/

    @Nullable
    private static Camera.Size getOptimalPreviewSize(@Nullable List<Camera.Size> sizes, int w, int h) {
        Log.v(TAG, "getOptimalPreviewSize()");
        if (sizes != null) {
            for (Camera.Size size : sizes) {
                Log.v(TAG, "-- size: " + size.width + "x" + size.height);
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int w = Math.abs(right - left);
        int h = Math.abs(bottom - top);

        Log.d(TAG, "onLayout(): " + w + "x" + h);

        if (!cameraPreviewAttached) {
            attachCameraView(w, h);
        }
    }

    public void permissionReceived() {
        requestLayout();
    }

    private void attachCameraView(int w, int h) {
        if (PermissionChecker.PERMISSION_GRANTED != PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.CAMERA)) {
            return;
        }
        cameraPreviewAttached = true;

        //int w = getMeasuredWidth();
        //int h = getMeasuredHeight();

        findOptimalSize(w, h, (optW, optH) -> {
            Log.v(TAG, "Optimal size for " + w + "x" + h + " is " + optW + "x" + optH);

            selectedPreviewSize[0] = optW;
            selectedPreviewSize[1] = optH;

            int surfaceW, surfaceH;

            if (getChildCount() == 0) {
                if (isPortraitMode()) {
                    surfaceW = w; // 1080
                    surfaceH = w * optW / optH; // 1080 * optW / optH; // TODO maybe h * ...
                } else {
                    surfaceW = h * optW / optH; // TODO maybe w * ...
                    surfaceH = h;
                }

                MarginLayoutParams layoutParams = new MarginLayoutParams(MeasureSpec.makeMeasureSpec(surfaceW, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(surfaceH, MeasureSpec.EXACTLY));
                if (isPortraitMode()) {
                    layoutParams.topMargin = -(surfaceH - h) / 2;
                } else {
                    MarginLayoutParamsCompat.setMarginStart(layoutParams, -(surfaceW - w) / 2);
                }

                cameraSurfaceView.getHolder().setFixedSize(surfaceW, surfaceH);
                addView(cameraSurfaceView, layoutParams);
            }
        });
    }

    public void setBarcodeProcessor(Detector.Processor<Barcode> processor) {
        this.barcodeProcessor = processor;
    }

    public boolean isDetectorOperational() {
        return barcodeDetector == null || barcodeDetector.isOperational();
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
        Log.v(TAG, "internalStart() [isPlaying=" + isPlaying + ", startRequested=" + startRequested + ", surfaceReady=" + surfaceReady + "]");
        if (isPlaying) {
            return;
        }

        if (startRequested && surfaceReady && cameraSource != null) {
            try {
                cameraSource.start(cameraSurfaceView.getHolder());
                isPlaying = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (cameraSource != null && isPlaying) {
            cameraSource.stop();
        }

        startRequested = false;
        isPlaying = false;
    }

    public void release() {
        internalRelease(false);
    }

    private void internalRelease(boolean hadStartRequest) {
        stop();

        startRequested = hadStartRequest;

        if (barcodeDetector != null) {
            barcodeDetector.release();
            barcodeDetector = null;
        }

        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    private Disposable findOptimalSize(int w, int h, OptimalSizeCallback callback) {
        Log.v(TAG, "Finding optimal size for " + w + "x" + h);
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "surfaceCreated()");
        surfaceReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG, "surfaceChanged() [width=" + width + ", height=" + height + "]");
        internalRelease(startRequested);

        barcodeDetector = new BarcodeDetector.Builder(getContext()).setBarcodeFormats(Barcode.QR_CODE).build();
        barcodeDetector.setProcessor(internalProcessor);

        CameraSource.Builder cameraSourceBuilder = new CameraSource.Builder(getContext(), barcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK);
        //.setRequestedPreviewSize(width, height);
        //.setRequestedFps(15f);

        if (selectedPreviewSize[0] > -1 && selectedPreviewSize[1] > -1) {
            cameraSourceBuilder.setRequestedPreviewSize(selectedPreviewSize[0], selectedPreviewSize[1]);
        }

        cameraSource = cameraSourceBuilder.build();

        internalStart();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "surfaceDestroyed()");
        release();
        surfaceReady = false;
    }

    private boolean isPortraitMode() {
        int orientation = getContext().getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private interface OptimalSizeCallback {
        void optimalSizeFound(int w, int h);
    }

}
