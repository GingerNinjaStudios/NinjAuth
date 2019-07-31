package me.gingerninja.authenticator.ui.security;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.StartupPasswordCheckFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;

public class StartupPasswordCheckFragment extends BaseFragment<StartupPasswordCheckFragmentBinding> {

    private OnBackPressedCallback backButtonCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            promptExit();
        }
    };

    private boolean isIntermediate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isIntermediate = StartupPasswordCheckFragmentArgs.fromBundle(requireArguments()).getIntermediate();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backButtonCallback);
        backButtonCallback.setEnabled(isIntermediate);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, StartupPasswordCheckFragmentBinding binding) {
        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);
        viewModel.setIntermediate(isIntermediate);

        viewModel.getEvents().observe(getViewLifecycleOwner(), this::handleEvents);
        binding.setViewModel(viewModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);

        if (!isIntermediate) {
            if (!viewModel.hasLock()) {
                viewModel.openUnlockedDatabase();
                getNavController().navigate(StartupPasswordCheckFragmentDirections.loginCompleteAction());
            } else {
                if (savedInstanceState == null) {
                    if (viewModel.enableBioAuth.get()) {
                        viewModel.bioAuthentication(this);
                    } else {
                        showPasswordKeyboard();
                    }
                }
            }
        }
    }

    private void showPasswordKeyboard() {
        getDataBinding().password.clearFocus();
        getDataBinding().password.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(getDataBinding().password, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void handleEvents(@NonNull SingleEvent event) {
        StartupPasswordCheckViewModel viewModel = getViewModel(StartupPasswordCheckViewModel.class);
        if (event.handle()) {
            switch (event.getId()) {
                case StartupPasswordCheckViewModel.EVENT_CONFIRM:
                    if (!isIntermediate) {
                        getNavController().navigate(StartupPasswordCheckFragmentDirections.loginCompleteAction());
                    } else {
                        getNavController().navigate(StartupPasswordCheckFragmentDirections.closeLoginScreenAsShieldAction());
                    }
                    /*if (viewModel.hasLock() && source == R.string.settings_security_bio_key) {
                        getNavController().navigate(PasswordCheckFragmentDirections.passwordCheckToBiometricsSetupAction(viewModel.password.get()));
                    } else {
                        getNavController().navigate(PasswordCheckFragmentDirections.openLockTypeSelectorAction(source, viewModel.password.get()));
                    }*/
                    break;
                case StartupPasswordCheckViewModel.EVENT_BIO_AUTH:
                    viewModel.bioAuthentication(this);
                    break;
                case StartupPasswordCheckViewModel.EVENT_USE_PASSWORD:
                    getDataBinding().password.post(this::showPasswordKeyboard);
                    break;
            }
        }
    }

    private void promptExit() {
        new ConfirmExitDialogFragment().show(requireFragmentManager(), ConfirmExitDialogFragment.TAG);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.startup_password_check_fragment;
    }

    public static class ConfirmExitDialogFragment extends DialogFragment {
        private static final String TAG = "exitConfirmDialog";

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.security_intermediate_confirm_exit_dialog_title)
                    .setMessage(R.string.security_intermediate_confirm_exit_dialog_message)
                    .setPositiveButton(R.string.security_intermediate_confirm_exit_dialog_confirm, this::onButtonClick)
                    .setNegativeButton(R.string.cancel, this::onButtonClick)
                    .create();
        }

        @SuppressWarnings("unused")
        private void onButtonClick(DialogInterface dialogInterface, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                requireActivity().finishAffinity();
            } else {
                dismissAllowingStateLoss();
            }
        }
    }
}
