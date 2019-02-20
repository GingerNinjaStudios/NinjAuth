package me.gingerninja.authenticator.ui.setup.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.SetupPageThemeSelectorBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.setup.SetupViewModel;
import me.gingerninja.authenticator.util.AppSettings;

public class ThemeSelectorPageFragment extends BaseFragment<SetupPageThemeSelectorBinding> {
    @Inject
    AppSettings appSettings;

    private SetupViewModel setupViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModel = ViewModelProviders.of(getParentFragment(), viewModelFactory).get(SetupViewModel.class);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, SetupPageThemeSelectorBinding binding) {
        binding.setViewModel(setupViewModel);

        binding.themeSpinner.setOnSpinnerChangeListener((newValue, oldValue) -> {
            appSettings.setTemporaryTheme((String) newValue);
            getActivity().recreate();
            return true;
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.setup_page_theme_selector;
    }
}
