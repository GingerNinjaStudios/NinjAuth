package me.gingerninja.authenticator.ui.backup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.BackupFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.backup.BackupUtils;

import static me.gingerninja.authenticator.ui.settings.SettingsFragment.RC_CREATE_BACKUP;

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
            case RC_CREATE_BACKUP:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = backupUtils.getUriFromIntent(data);
                    /*backupUtils.backup(uri).subscribe(() -> {
                        Snackbar.make(getView(), "Backup created successfully", Snackbar.LENGTH_LONG).show();
                    }, throwable -> {
                        Timber.e(throwable, "Cannot create backup: %s", throwable.getMessage());
                        Snackbar.make(getView(), "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                    });*/
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void pickFile() {
        backupUtils.createFile(this, RC_CREATE_BACKUP, "ninjauth-backup.zip", false);
    }
}
