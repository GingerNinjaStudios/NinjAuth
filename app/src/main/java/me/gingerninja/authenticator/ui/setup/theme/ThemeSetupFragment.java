package me.gingerninja.authenticator.ui.setup.theme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.SetupPageThemeSelectorBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.setup.SkipConfirmationBottomFragment;
import me.gingerninja.authenticator.util.AppSettings;

public class ThemeSetupFragment extends BaseFragment<SetupPageThemeSelectorBinding> implements SkipConfirmationBottomFragment.SkipDialogListener {
    @Inject
    AppSettings appSettings;

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, SetupPageThemeSelectorBinding binding) {
        binding.setViewModel(getViewModel(ThemeSetupViewModel.class));
        binding.btnNext.setOnClickListener(this::onNextClick);
        binding.btnSkip.setOnClickListener(this::onSkipClick);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.setup_page_theme_selector;
    }

    private void onSkipClick(View view) {
        SkipConfirmationBottomFragment.show(getChildFragmentManager());
    }

    private void onNextClick(View view) {
        getNavController().navigate(ThemeSetupFragmentDirections.openSecuritySetupAction());
    }

    @Override
    public void onSkipSetup() {
        appSettings.setFirstRunComplete();
        getNavController().navigate(ThemeSetupFragmentDirections.skipSetupFromThemeAction());
    }
}
