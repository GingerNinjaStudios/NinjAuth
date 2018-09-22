package me.gingerninja.authenticator.ui.account.camera;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.databinding.AccountFromCameraFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.Parser;
import me.gingerninja.authenticator.util.RequestCodes;

public class AddAccountFromCameraFragment extends BaseFragment<AccountFromCameraFragmentBinding> implements Detector.Processor<Barcode> {
    private AtomicBoolean detectionEnabled = new AtomicBoolean(true);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountFromCameraFragmentBinding viewDataBinding) {
    }

    private void openCamera() {
        Context context = getContext();

        if (PermissionChecker.PERMISSION_GRANTED != PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, RequestCodes.PERMISSION_CAMERA);
            return;
        }

        CameraPreviewFrame cameraPreview = getDataBinding().cameraPreview;

        if (cameraPreview.isDetectorOperational()) {
            cameraPreview.setBarcodeProcessor(this);
            cameraPreview.start();
        } else {
            // TODO wait until operational
        }
    }

    private void stopDetection() {
        if (detectionEnabled.compareAndSet(true, false)) {
            Single.just(getDataBinding().cameraPreview)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(c -> c.release());
        }
        //getDataBinding().cameraPreview.releaseCamera();
    }

    @Override
    public void onStart() {
        super.onStart();
        openCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        getDataBinding().cameraPreview.stop();
    }

    @Override
    public void onDestroyView() {
        getDataBinding().cameraPreview.release();
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCodes.PERMISSION_CAMERA:
                openCamera();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.account_from_camera_fragment;
    }

    @Override
    public void release() {
    }

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        if (!detectionEnabled.get()) {
            return;
        }

        if (detections.detectorIsOperational()) {
            SparseArray<Barcode> items = detections.getDetectedItems();
            for (int i = 0; i < items.size(); i++) {
                Barcode barcode = items.valueAt(i);
                Log.d("AddAccountFromCamera", "barcode: " + barcode.rawValue + ", valueFormat: " + barcode.valueFormat);
                Account account = Parser.parseUrl(barcode.rawValue);

                if (account != null) {
                    Snackbar.make(getView(), account.getAccountName() + " by " + account.getIssuer() + " found", Snackbar.LENGTH_LONG).show();
                    // TODO found QR code
                    stopDetection();
                    getNavController().popBackStack();
                }
            }

        } else {
            // TODO show wait for operational status
        }
    }
}
