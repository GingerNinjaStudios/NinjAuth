package me.gingerninja.authenticator.ui.setup.security;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.databinding.SetupPageSecurityBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.setup.SkipConfirmationBottomFragment;
import me.gingerninja.authenticator.util.AppSettings;

public class SecuritySetupFragment extends BaseFragment<SetupPageSecurityBinding> implements SkipConfirmationBottomFragment.SkipDialogListener {
    @Inject
    Crypto crypto;

    @Inject
    AppSettings appSettings;

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, SetupPageSecurityBinding binding) {
        binding.setViewModel(getViewModel(SecuritySetupViewModel.class));
        binding.btnNext.setOnClickListener(this::onSkipClick);
        binding.btnSecuritySetup.setOnClickListener(this::onStartSecuritySetupClick);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (crypto.hasLock()) {
            appSettings.setFirstRunComplete();
            getNavController().navigate(SecuritySetupFragmentDirections.openSetupCompleteAction());
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.setup_page_security;
    }

    private void onStartSecuritySetupClick(View view) {
        final int source = crypto.getFeatures().isBiometricsSupported() ? R.string.settings_security_bio_key : R.string.settings_security_lock_key;
        getNavController().navigate(SecuritySetupFragmentDirections.setLockFromSetupAction(source));
    }

    private void onSkipClick(View view) {
        SkipConfirmationBottomFragment.show(getChildFragmentManager());
    }

    @Override
    public void onSkipSetup() {
        appSettings.setFirstRunComplete();
        getNavController().navigate(SecuritySetupFragmentDirections.openSetupCompleteAction());
    }
}
