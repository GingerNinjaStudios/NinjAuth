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

        CameraPreviewFrame cameraPreview = getDataBinding().cameraPreview;

        cameraPreview.setBarcodeProcessor(this);
        cameraPreview.start();

        if (PermissionChecker.PERMISSION_GRANTED != PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, RequestCodes.PERMISSION_CAMERA);
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
                if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                    getDataBinding().cameraPreview.permissionReceived();
                } else {
                    getNavController().popBackStack();
                }
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
                String url = barcode.rawValue;

                Log.d("AddAccountFromCamera", "barcode: " + url + ", valueFormat: " + barcode.valueFormat);
                Account account = Parser.parseUrl(url);

                if (account != null) {
                    // TODO found QR code
                    stopDetection();
                    AddAccountFromCameraFragmentDirections.CreateNewAccountFromCameraAction action = AddAccountFromCameraFragmentDirections.createNewAccountFromCameraAction().setUrl(url);
                    navigateForResultTransfer().navigate(action);
                }
            }

        } else {
            // TODO show wait for operational status
        }
    }
}
