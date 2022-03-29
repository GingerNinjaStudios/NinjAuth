package me.gingerninja.authenticator.ui.backup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.BackupFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.RequestCodes;
import me.gingerninja.authenticator.util.backup.BackupUtils;

public class BackupFragment extends BaseFragment<BackupFragmentBinding> {
    @Inject
    BackupUtils backupUtils;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getViewModel(BackupViewModel.class)
                .getEvents()
                .observe(this, event -> {
                    if (event.handle()) {
                        switch (event.getId()) {
                            case BackupViewModel.EVENT_CREATE_BACKUP:
                                pickFile();
                                break;
                        }
                    }
                });
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, BackupFragmentBinding binding) {
        binding.setViewModel(getViewModel(BackupViewModel.class));
        binding.toolbar.setNavigationOnClickListener(v -> getNavController().navigateUp());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.backup_fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCodes.BACKUP:
                if (resultCode == Activity.RESULT_OK) {
                    getViewModel(BackupViewModel.class).handleBackupPickerResults(data);
                    new BackupDialogFragment().show(getChildFragmentManager(), "backupDialogFragment");
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void pickFile() {
        String datePart = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        backupUtils.createFile(this, RequestCodes.BACKUP, "ninjauth-backup-" + datePart + ".zip", false);
    }
}
