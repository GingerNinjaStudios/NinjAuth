package me.gingerninja.authenticator.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceScreen;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.SettingsFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.backup.BackupUtils;
import timber.log.Timber;

public class SettingsFragment extends BaseFragment<SettingsFragmentBinding> implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    public static final int RC_CREATE_BACKUP = 0x3010;
    public static final int RC_RESTORE = 0x3020;

    @Inject
    BackupUtils backupUtils;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            SettingsScreenFragment fragment = new SettingsScreenFragment();

            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.replace(R.id.settings_fragment, fragment);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, SettingsFragmentBinding binding) {
        binding.toolbar.setNavigationOnClickListener(v -> {
            FragmentManager fm = getChildFragmentManager();
            if (fm.getBackStackEntryCount() == 0) {
                getNavController().navigateUp();
            } else {
                fm.popBackStack();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.settings_fragment;
    }

    @Override
    public boolean onPreferenceStartScreen(androidx.preference.PreferenceFragmentCompat caller, PreferenceScreen preferenceScreen) {
        SettingsScreenFragment fragment = new SettingsScreenFragment();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.settings_fragment, fragment, preferenceScreen.getKey());
        ft.addToBackStack(preferenceScreen.getKey());
        ft.commitAllowingStateLoss();

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_CREATE_BACKUP:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = backupUtils.getUriFromIntent(data);
                    backupUtils.backup(uri).subscribe(() -> {
                        Snackbar.make(getView(), "Backup created successfully", Snackbar.LENGTH_LONG).show();
                    }, throwable -> {
                        Timber.e(throwable, "Cannot create backup: %s", throwable.getMessage());
                        Snackbar.make(getView(), "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
                }
                break;
            case RC_RESTORE:
                if (resultCode == Activity.RESULT_OK && data != null) {

                    Uri uri = backupUtils.getUriFromIntent(data);
                    if (uri != null) {
                        SettingsFragmentDirections.SettingsToRestoreFragmentAction restoreAction = new SettingsFragmentDirections.SettingsToRestoreFragmentAction(uri);
                        navigateForResult(RC_RESTORE).navigate(restoreAction);
                    }

                    /*Uri uri = backupUtils.getUriFromIntent(data);

                    backupUtils.restore(uri)
                            .prepare()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(restore -> {
                                if (restore.isPasswordNeeded()) {
                                    PasswordDialogFragment.show(getChildFragmentManager());
                                } else {
                                    restore.restore(null).blockingGet();
                                }
                            }, throwable -> {
                                Timber.e(throwable, "Cannot restore: %s", throwable.getMessage());
                                Snackbar.make(getView(), "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                            });*/
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Object data) {
        switch (requestCode) {
            case RC_CREATE_BACKUP:
                // TODO
                break;
            case RC_RESTORE:
                // TODO
                break;
        }
    }
}
