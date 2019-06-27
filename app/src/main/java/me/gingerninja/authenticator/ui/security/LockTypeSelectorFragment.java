package me.gingerninja.authenticator.ui.security;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.ui.settings.BaseSettingsFragment;

public class LockTypeSelectorFragment extends BaseSettingsFragment implements LockTypeConfirmationListener {
    private static final String CONFIRM_DIALOG_TAG = "lockTypeConfirmDialog";

    private static final int[] ID_VALUES = {
            R.string.settings_prot_none_value,
            R.string.settings_prot_pin_value,
            R.string.settings_prot_password_value,
            R.string.settings_prot_bio_pin_value,
            R.string.settings_prot_bio_password_value
    };

    private final OnBackPressedCallback backButtonCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            exit();
        }
    };

    //private int source;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*LockTypeSelectorFragmentArgs args = LockTypeSelectorFragmentArgs.fromBundle(requireArguments());
        source = args.getSource();*/

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backButtonCallback);

        LockTypeSelectorViewModel viewModel = getViewModel(LockTypeSelectorViewModel.class);
        viewModel.getEvents().observe(this, event -> {
            if(event.handle()){
                switch(event.getId()){
                    case LockTypeSelectorViewModel.EVENT_LOCK_REMOVED:
                        exit();
                        break;
                }
            }
        });
    }

    @Override
    protected void onNavigateUpClicked() {
        exit();
    }

    @Override
    protected void onPreferencesCreated(@Nullable Bundle savedInstanceState, String rootKey) {
        LockTypeSelectorViewModel viewModel = getViewModel(LockTypeSelectorViewModel.class);
        String currentKey = viewModel.getLockType();

        Preference pref = findPreference(currentKey);
        if (pref != null) {
            pref.setSummary(R.string.security_lock_type_current);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        final int id = getIdFromKey(key);
        LockTypeSelectorViewModel viewModel = getViewModel(LockTypeSelectorViewModel.class);
        String currentKey = viewModel.getLockType();

        if (id == R.string.settings_prot_none_value) {
            if (!Crypto.PROTECTION_MODE_NONE.equals(currentKey)) {
                new LockTypeRemoveConfirmDialog().show(getChildFragmentManager(), CONFIRM_DIALOG_TAG);
            } else {
                exit();
            }
        } else {
            String pass = LockTypeSelectorFragmentArgs.fromBundle(requireArguments()).getPass();
            LockTypeSelectorFragmentDirections.OpenPasswordSetFragmentAction action = LockTypeSelectorFragmentDirections.openPasswordSetFragmentAction(id, pass);
            getNavController().navigate(action);
        }

        return true;
    }

    @StringRes
    private int getIdFromKey(@NonNull String key) {
        for (int id : ID_VALUES) {
            if (getString(id).equals(key)) {
                return id;
            }
        }

        return 0;
    }

    private void exit() {
        getNavController().navigate(LockTypeSelectorFragmentDirections.exitLockTypeSelectorFragment());
    }

    @Override
    protected String getTitle() {
        return getString(R.string.security_lock_type_title);
    }

    @Override
    protected int getSettingsXmlId() {
        LockTypeSelectorFragmentArgs args = LockTypeSelectorFragmentArgs.fromBundle(requireArguments());
        int source = args.getSource();

        if (source == R.string.settings_security_bio_key) {
            return R.xml.lock_type_selector_bio;
        }
        return R.xml.lock_type_selector_normal;
    }

    @Override
    public void onLockTypeRemoveConfirm() {
        String pass = LockTypeSelectorFragmentArgs.fromBundle(requireArguments()).getPass();
        getViewModel(LockTypeSelectorViewModel.class).removeLock(pass.toCharArray());
    }

    public static class LockTypeRemoveConfirmDialog extends DialogFragment {
        private LockTypeConfirmationListener listener;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);

            if (getParentFragment() instanceof LockTypeConfirmationListener) {
                listener = (LockTypeConfirmationListener) getParentFragment();
            } else if (context instanceof LockTypeConfirmationListener) {
                listener = (LockTypeConfirmationListener) context;
            }
        }

        @Override
        public void onDetach() {
            listener = null;
            super.onDetach();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.security_lock_type_confirm_dialog_title)
                    .setMessage(R.string.security_lock_type_confirm_dialog_message)
                    .setPositiveButton(R.string.security_lock_type_confirm_dialog_confirm, this::onButtonClick)
                    .setNegativeButton(R.string.cancel, this::onButtonClick)
                    .create();
        }

        @SuppressWarnings("unused")
        private void onButtonClick(DialogInterface dialogInterface, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                listener.onLockTypeRemoveConfirm();
            } else {
                dismissAllowingStateLoss();
            }
        }
    }

}
