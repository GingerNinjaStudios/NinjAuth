package me.gingerninja.authenticator.ui.backup;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.AndroidSupportInjection;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.viewmodel.NinjaViewModelFactory;

public class RestorePasswordDialogFragment extends DialogFragment {
    public static final String EXTRA_WRONG_PASSWORD = "wrong-password";
    private static final String TAG = "password-for-restore-dialog";

    @Inject
    NinjaViewModelFactory viewModelFactory;

    private RestoreViewModel settingsViewModel;

    public static RestorePasswordDialogFragment show(FragmentManager fragmentManager, boolean wrongPassword) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_WRONG_PASSWORD, wrongPassword);

        RestorePasswordDialogFragment fragment = new RestorePasswordDialogFragment();
        fragment.setArguments(args);
        fragment.show(fragmentManager, TAG);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
        settingsViewModel = ViewModelProviders.of(getParentFragment(), viewModelFactory).get(RestoreViewModel.class);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        settingsViewModel.cancelRestore();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context ctx = requireContext();

        View root = LayoutInflater.from(ctx).inflate(R.layout.restore_password_dialog_fragment, null);

        Bundle args = getArguments();

        TextInputLayout til = root.findViewById(R.id.textinput_layout);

        if (args != null && args.getBoolean(EXTRA_WRONG_PASSWORD, false)) {
            til.setErrorEnabled(true);
            til.setError(ctx.getString(R.string.settings_restore_password_dialog_wrong_pass));
        }

        AlertDialog d = new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.settings_restore_password_dialog_title)
                .setView(root)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    TextInputEditText editText = root.findViewById(R.id.password);
                    settingsViewModel.continueRestore(editText.getText().toString().toCharArray());
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    settingsViewModel.cancelRestore();
                })
                .create();

        d.setOnShowListener(dialog -> {
            EditText editText = til.getEditText();
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && editText != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        return d;
    }
}
