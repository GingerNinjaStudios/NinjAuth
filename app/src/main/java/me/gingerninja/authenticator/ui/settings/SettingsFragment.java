package me.gingerninja.authenticator.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceScreen;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.SettingsFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class SettingsFragment extends BaseFragment<SettingsFragmentBinding> implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
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
}
