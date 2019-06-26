package me.gingerninja.authenticator.ui.security;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.preference.Preference;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.ui.settings.BaseSettingsFragment;

public class LockTypeSelectorFragment extends BaseSettingsFragment {
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

    private int source;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LockTypeSelectorFragmentArgs args = LockTypeSelectorFragmentArgs.fromBundle(requireArguments());
        source = args.getSource();

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backButtonCallback);
    }

    @Override
    protected void onNavigateUpClicked() {
        exit();
    }

    @Override
    protected void onPreferencesCreated(@Nullable Bundle savedInstanceState, String rootKey) {
        String currentKey = getString(R.string.settings_prot_none_value); // TODO get from sharedprefs
        Preference pref = findPreference(currentKey);
        if (pref != null) {
            pref.setSummary(R.string.security_lock_type_current);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        final int id = getIdFromKey(key);

        if (id == R.string.settings_prot_none_value) {
            // TODO
            exit();
        } else {
            LockTypeSelectorFragmentDirections.OpenPasswordSetFragmentAction action = LockTypeSelectorFragmentDirections.openPasswordSetFragmentAction(id);
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
}
