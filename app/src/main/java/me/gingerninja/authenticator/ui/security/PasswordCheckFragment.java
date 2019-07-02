package me.gingerninja.authenticator.ui.security;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.PasswordCheckFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;

public class PasswordCheckFragment extends BaseFragment<PasswordCheckFragmentBinding> {
    private final OnBackPressedCallback backButtonCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            exit();
        }
    };

    private int source;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PasswordCheckFragmentArgs args = PasswordCheckFragmentArgs.fromBundle(requireArguments());
        source = args.getSource();
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, PasswordCheckFragmentBinding binding) {
        PasswordCheckViewModel viewModel = getViewModel(PasswordCheckViewModel.class);
        binding.toolbar.setNavigationOnClickListener(view -> exit());

        viewModel.getEvents().observe(getViewLifecycleOwner(), this::handleEvents);
        binding.setViewModel(viewModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PasswordCheckViewModel viewModel = getViewModel(PasswordCheckViewModel.class);

        if (!viewModel.hasLock()) {
            getNavController().navigate(PasswordCheckFragmentDirections.openLockTypeSelectorAction(source, null));
        } else {
            requireActivity().getOnBackPressedDispatcher().addCallback(this, backButtonCallback);

            if (savedInstanceState == null) {
                getDataBinding().password.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(getDataBinding().password, 0);
                }
            }
        }
    }

    private void exit() {
        PasswordCheckViewModel viewModel = getViewModel(PasswordCheckViewModel.class);

        if (viewModel.inputEnabled.get()) {
            getNavController().navigateUp();
        }
    }

    private void handleEvents(@NonNull SingleEvent event) {
        PasswordCheckViewModel viewModel = getViewModel(PasswordCheckViewModel.class);
        if (event.handle()) {
            switch (event.getId()) {
                case PasswordCheckViewModel.EVENT_CONFIRM:
                    if (viewModel.hasLock() && source == R.string.settings_security_bio_key) {
                        getNavController().navigate(PasswordCheckFragmentDirections.passwordCheckToBiometricsSetupAction(viewModel.password.get()));
                    } else {
                        getNavController().navigate(PasswordCheckFragmentDirections.openLockTypeSelectorAction(source, viewModel.password.get()));
                    }
                    break;
                case PasswordCheckViewModel.EVENT_BIO_AUTH:
                    // TODO
                    break;
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.password_check_fragment;
    }
}
