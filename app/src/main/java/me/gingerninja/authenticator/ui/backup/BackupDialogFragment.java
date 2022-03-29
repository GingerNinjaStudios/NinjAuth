package me.gingerninja.authenticator.ui.backup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.BackupDialogFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseDialogFragment;

public class BackupDialogFragment extends BaseDialogFragment<BackupDialogFragmentBinding> {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackupViewModel backupViewModel = new ViewModelProvider(requireParentFragment(), viewModelFactory).get(BackupViewModel.class);
        BackupDialogViewModel viewModel = getViewModel(BackupDialogViewModel.class);
        viewModel.setupWithParentViewModel(backupViewModel);

        viewModel.getEvents().observe(this, event -> {
            if (event.handle()) {
                switch (event.getId()) {
                    case BackupDialogViewModel.EVENT_DISMISS_SUCCESS:
                        //dismissAllowingStateLoss();
                        getNavController().popBackStack(R.id.settingsFragment, false);
                        break;
                    case BackupDialogViewModel.EVENT_DISMISS_ERROR:
                        dismissAllowingStateLoss();
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, BackupDialogFragmentBinding binding) {
        binding.setViewModel(getViewModel(BackupDialogViewModel.class));
    }

    @Override
    protected void onCreateDialog(@NonNull MaterialAlertDialogBuilder builder) {
        builder.setCancelable(false);
        setCancelable(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.backup_dialog_fragment;
    }
}
