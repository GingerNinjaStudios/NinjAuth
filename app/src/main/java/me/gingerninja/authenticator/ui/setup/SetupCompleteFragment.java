package me.gingerninja.authenticator.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.psdev.licensesdialog.LicensesDialogFragment;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.SetupCompleteFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class SetupCompleteFragment extends BaseFragment<SetupCompleteFragmentBinding> {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getViewModel(SetupCompleteViewModel.class);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, SetupCompleteFragmentBinding binding) {
        binding.btnNext.setOnClickListener(this::onStartClick);
        binding.btnLicense.setOnClickListener(this::onLicenseClick);
    }

    private void onStartClick(View v) {
        getNavController().navigate(SetupCompleteFragmentDirections.finishSetupAction());
    }

    private void onLicenseClick(View v) {
        Notice appNotice = new Notice();
        appNotice.setName(getString(R.string.app_name));
        appNotice.setCopyright("Copyright 2019 Gergely Kőrössy");
        appNotice.setLicense(new MITLicense());
        appNotice.setUrl("https://www.ninjauth.app");

        new LicensesDialogFragment.Builder(requireContext())
                .setNotice(appNotice)
                .setIncludeOwnLicense(false)
                .build()
                .show(getChildFragmentManager(), "license");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.setup_complete_fragment;
    }
}
