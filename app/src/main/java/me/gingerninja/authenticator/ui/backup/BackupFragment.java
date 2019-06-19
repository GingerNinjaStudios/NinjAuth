package me.gingerninja.authenticator.ui.backup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.BackupFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.RequestCodes;
import me.gingerninja.authenticator.util.backup.Backup;
import me.gingerninja.authenticator.util.backup.BackupUtils;
import timber.log.Timber;

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
                    Uri uri = backupUtils.getUriFromIntent(data);

                    BackupViewModel.Data vmData = getViewModel(BackupViewModel.class).data;
                    String rawPass = vmData.pass.get();
                    char[] pass = TextUtils.isEmpty(rawPass) ? null : rawPass.toCharArray();

                    Backup.Options options = new Backup.Options.Builder()
                            .password(pass)
                            .withAccountImages(vmData.accountImages.get())
                            .setComment(vmData.comment.get())
                            .setAutoBackup(false)
                            .build();

                    backupUtils.backup(uri)
                            .export(options)
                            .subscribe(() -> {
                                Snackbar.make(getView(), "Backup created successfully", Snackbar.LENGTH_LONG).show();
                                //setResultAndLeave(RESULT_OK);
                            }, throwable -> {
                                Timber.e(throwable, "Cannot create backup: %s", throwable.getMessage());
                                Snackbar.make(getView(), "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                            });
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
        backupUtils.createFile(this, RequestCodes.BACKUP, "ninjauth-backup.zip", false);
    }
}
