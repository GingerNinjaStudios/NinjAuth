package me.gingerninja.authenticator.ui.security;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.BiometricsSetFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;

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
            }
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
