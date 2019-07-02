package me.gingerninja.authenticator.ui.security;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.StartupPasswordCheckFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;

public class StartupPasswordCheckFragment extends BaseFragment<StartupPasswordCheckFragmentBinding> {

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, StartupPasswordCheckFragmentBinding binding) {
        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);

        viewModel.getEvents().observe(getViewLifecycleOwner(), this::handleEvents);
        binding.setViewModel(viewModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);

        if (!viewModel.hasLock()) {
            viewModel.openUnlockedDatabase();
            getNavController().navigate(StartupPasswordCheckFragmentDirections.loginCompleteAction());
        } else {
            if (savedInstanceState == null) {
                if (viewModel.enableBioAuth.get()) {
                    viewModel.bioAuthentication(this);
                } else {
                    getDataBinding().password.clearFocus();
                    getDataBinding().password.requestFocus();
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(getDataBinding().password, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }
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
                    viewModel.bioAuthentication(this);
                    break;
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.startup_password_check_fragment;
    }
}
