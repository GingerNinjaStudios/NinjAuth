package me.gingerninja.authenticator.ui.security;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.PasswordSetFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;

public class PasswordSetFragment extends BaseFragment<PasswordSetFragmentBinding> {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            int type = PasswordSetFragmentArgs.fromBundle(requireArguments()).getType();
            boolean usePin = type == R.string.settings_prot_pin_value || type == R.string.settings_prot_bio_pin_value;
            getViewModel(PasswordSetViewModel.class).setUsePin(usePin);
        }
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, PasswordSetFragmentBinding binding) {
        binding.toolbar.setNavigationOnClickListener(view -> getNavController().navigateUp());

        PasswordSetViewModel viewModel = getViewModel(PasswordSetViewModel.class);
        viewModel.getEvents().observe(getViewLifecycleOwner(), this::handleEvents);
        binding.setViewModel(viewModel);
    }

    private void handleEvents(@NonNull SingleEvent event) {
        if (event.handle()) {
            if (PasswordSetViewModel.EVENT_NEXT.equals(event.getId())) {
                // TODO navigate
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.password_set_fragment;
    }
}
