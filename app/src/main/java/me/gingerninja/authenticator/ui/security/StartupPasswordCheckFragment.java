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
import me.gingerninja.authenticator.databinding.StartupPasswordCheckFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;

public class StartupPasswordCheckFragment extends BaseFragment<StartupPasswordCheckFragmentBinding> {
    private final OnBackPressedCallback backButtonCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            exit();
        }
    };

    private boolean leaving;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);

        if (!viewModel.hasLock()) {
            leaving = true;
            viewModel.openUnlockedDatabase();
            getNavController().navigate(StartupPasswordCheckFragmentDirections.loginCompleteAction());
        } else {
            requireActivity().getOnBackPressedDispatcher().addCallback(this, backButtonCallback);
        }
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, StartupPasswordCheckFragmentBinding binding) {
        if (leaving) {
            return;
        }

        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);
        binding.toolbar.setNavigationOnClickListener(view -> exit());

        viewModel.getEvents().observe(getViewLifecycleOwner(), this::handleEvents);
        binding.setViewModel(viewModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (leaving) {
            return;
        }

        if (savedInstanceState == null) {
            getDataBinding().password.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(getDataBinding().password, 0);
            }
        }
    }

    private void exit() {
        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);

        if (viewModel.inputEnabled.get()) {
            getNavController().navigate(StartupPasswordCheckFragmentDirections.leaveLoginScreen());
        }
    }

    private void handleEvents(@NonNull SingleEvent event) {
        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);
        if (event.handle()) {
            switch (event.getId()) {
                case StartupPasswordCheckViewModel.EVENT_CONFIRM:
                    getNavController().navigate(StartupPasswordCheckFragmentDirections.loginCompleteAction());
                    /*if (viewModel.hasLock() && source == R.string.settings_security_bio_key) {
                        getNavController().navigate(PasswordCheckFragmentDirections.passwordCheckToBiometricsSetupAction(viewModel.password.get()));
                    } else {
                        getNavController().navigate(PasswordCheckFragmentDirections.openLockTypeSelectorAction(source, viewModel.password.get()));
                    }*/
                    break;
                case StartupPasswordCheckViewModel.EVENT_BIO_AUTH:
                    // TODO
                    break;
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.startup_password_check_fragment;
    }
}
