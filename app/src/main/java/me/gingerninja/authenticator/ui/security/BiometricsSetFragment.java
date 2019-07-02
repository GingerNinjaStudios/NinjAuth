package me.gingerninja.authenticator.ui.security;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.BiometricsSetFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.RequestCodes;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public class BiometricsSetFragment extends BaseFragment<BiometricsSetFragmentBinding> {
    private final OnBackPressedCallback backButtonCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            exit();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backButtonCallback);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, BiometricsSetFragmentBinding binding) {
        binding.toolbar.setNavigationOnClickListener(view -> exit());

        BiometricsSetViewModel viewModel = getViewModel(BiometricsSetViewModel.class);
        viewModel.getEvents().observe(getViewLifecycleOwner(), this::handleEvents);
        binding.setViewModel(viewModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Drawable drawable = dataBinding.lockImg.getDrawable();

        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    private void handleEvents(@NonNull SingleEvent event) {
        BiometricsSetViewModel viewModel = getViewModel(BiometricsSetViewModel.class);
        char[] pass = BiometricsSetFragmentArgs.fromBundle(requireArguments()).getPass().toCharArray();

        if (event.handle()) {
            switch (event.getId()) {
                case BiometricsSetViewModel.EVENT_ENABLE:
                    viewModel.createBiometrics(this, pass);
                    break;
                case BiometricsSetViewModel.EVENT_ENABLE_SUCCESS:
                case BiometricsSetViewModel.EVENT_DISABLE_SUCCESS:
                    exit();
                    break;
                case BiometricsSetViewModel.EVENT_DISABLE:
                    viewModel.removeBiometrics();
                    break;
                case BiometricsSetViewModel.EVENT_SKIP:
                    exit();
                    break;
                case BiometricsSetViewModel.EVENT_BIO_ERROR_ENROLL:
                    openEnrollSettings();
                    break;
                case BiometricsSetViewModel.EVENT_BIO_ERROR_UNLOCK:
                    openDeviceLockScreen();
                    break;
            }
        }
    }

    private void openEnrollSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            intent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
        } else {
            intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
                intent = new Intent(Settings.ACTION_SETTINGS);
            }
        }

        startActivityForResult(intent, RequestCodes.SECURITY_ENROLL);
    }

    private void openDeviceLockScreen() {
        KeyguardManager keyguardManager = (KeyguardManager) requireContext().getSystemService(Context.KEYGUARD_SERVICE);

        if (keyguardManager != null) {
            keyguardManager.createConfirmDeviceCredentialIntent(null, null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Timber.v("ACTIVITY RESULT: request: %d, result: %d, data: %s", requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCodes.SECURITY_ENROLL:
            case RequestCodes.SECURITY_CONFIRM_DEVICE_CREDENTIAL:
                char[] pass = BiometricsSetFragmentArgs.fromBundle(requireArguments()).getPass().toCharArray();
                getViewModel(BiometricsSetViewModel.class).createBiometrics(this, pass);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void exit() {
        getNavController().navigate(BiometricsSetFragmentDirections.exitBiometricsSetFragmentAction());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.biometrics_set_fragment;
    }
}
