package me.gingerninja.authenticator.ui.settings;


import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.XmlRes;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.AppSettings;
import me.gingerninja.authenticator.util.resulthandler.FragmentResultListener;

public abstract class BaseSettingsFragment extends PreferenceFragmentCompat implements FragmentResultListener {
    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        if (root != null) {
            Toolbar toolbar = root.findViewById(R.id.toolbar);
            toolbar.setNavigationOnClickListener(view -> onNavigateUpClicked());

            TextView toolbarTitle = root.findViewById(R.id.toolbar_title);
            toolbarTitle.setText(getTitle());
        }

        return root;
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getSharedPreferencesName());
        setPreferencesFromResource(getSettingsXmlId(), rootKey);

        onPreferencesCreated(savedInstanceState, rootKey);
    }

    protected void onPreferencesCreated(@Nullable Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            IBinder token = requireActivity().findViewById(android.R.id.content).getWindowToken();
            imm.hideSoftInputFromWindow(token, 0);
        }
    }

    @NonNull
    protected <U extends ViewModel> U getViewModel(@NonNull Class<U> modelClass) {
        return ViewModelProviders.of(this, viewModelFactory).get(modelClass);
    }

    public NavController getNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    protected void onNavigateUpClicked() {
        getNavController().navigateUp();
    }

    protected abstract String getTitle();

    @XmlRes
    protected abstract int getSettingsXmlId();

    protected String getSharedPreferencesName() {
        return AppSettings.SHARED_PREFS_NAME;
    }

    protected boolean isPreferenceKey(@Nullable String key, @StringRes int keyRes) {
        return getString(keyRes).equals(key);
    }

    @Nullable
    public <T extends Preference> T findPreference(@StringRes int keyResId) {
        return findPreference(getString(keyResId));
    }

    @NonNull
    public <T extends Preference> T requirePreference(@StringRes int keyResId) {
        return requirePreference(getString(keyResId));
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public <T extends Preference> T requirePreference(@NonNull CharSequence key) {
        Preference preference = findPreference(key);
        if (preference == null) {
            throw new IllegalStateException("Preference not found for key '" + key + "'");
        }
        return (T) preference;
    }
}
