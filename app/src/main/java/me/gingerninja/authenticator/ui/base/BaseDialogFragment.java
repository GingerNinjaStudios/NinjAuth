package me.gingerninja.authenticator.ui.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.DaggerDialogFragment;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.resulthandler.FragmentResultListener;

public abstract class BaseDialogFragment<T extends ViewDataBinding> extends DialogFragment implements FragmentResultListener {
    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    protected T dataBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        onCreateView(inflater, container, savedInstanceState, dataBinding.getRoot(), dataBinding);
        return dataBinding.getRoot();
    }

    protected abstract void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, T viewDataBinding);

    @SuppressWarnings("unchecked")
    protected T getDataBinding() {
        return dataBinding;
    }

    public NavController getNavController() {
        return Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
    }

    @NonNull
    protected <U extends ViewModel> U getViewModel(@NonNull Class<U> modelClass) {
        return new ViewModelProvider(this, viewModelFactory).get(modelClass);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        onCreateDialog(builder);
        builder.setView(createView(LayoutInflater.from(requireContext()), null, savedInstanceState));
        return builder.create();
    }

    protected void onCreateDialog(@NonNull MaterialAlertDialogBuilder builder) {
        // override this if customization is needed
    }

    @LayoutRes
    protected abstract int getLayoutId();
}
