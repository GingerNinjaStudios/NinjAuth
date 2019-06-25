package me.gingerninja.authenticator.ui.security;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executors;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.BiometricsSetFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public class BiometricsSetFragment extends BaseFragment<BiometricsSetFragmentBinding> {
    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, BiometricsSetFragmentBinding binding) {
        binding.toolbar.setNavigationOnClickListener(view -> getNavController().navigateUp());

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
        if (event.handle()) {
            switch (event.getId()) {
                case BiometricsSetViewModel.EVENT_AUTH:
                    BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Authentication")
                            .setNegativeButtonText(getString(android.R.string.cancel))
                            .build();

                    BiometricPrompt prompt = new BiometricPrompt(requireActivity(), Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Timber.e("Biometric error, code: %d, msg: %s", errorCode, errString);
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Timber.e("Biometric success: %s", result);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Timber.e("Biometric failed");
                        }
                    });

                    prompt.authenticate(promptInfo);
                    break;
                case BiometricsSetViewModel.EVENT_SKIP:
                    getNavController().navigateUp();
                    break;
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.biometrics_set_fragment;
    }
}
