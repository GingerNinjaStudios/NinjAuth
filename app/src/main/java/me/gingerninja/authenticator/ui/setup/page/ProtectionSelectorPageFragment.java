package me.gingerninja.authenticator.ui.setup.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.SetupPageThemeSelectorBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class ProtectionSelectorPageFragment extends BaseFragment<SetupPageThemeSelectorBinding> {
    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, SetupPageThemeSelectorBinding binding) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.setup_page_theme_selector;
    }
}
