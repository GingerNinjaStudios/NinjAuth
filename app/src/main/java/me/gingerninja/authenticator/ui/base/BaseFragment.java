package me.gingerninja.authenticator.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.DaggerFragment;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.resulthandler.FragmentResultListener;

public abstract class BaseFragment<T extends ViewDataBinding> extends DaggerFragment implements FragmentResultListener {
    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    protected T dataBinding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        onCreateView(inflater, container, savedInstanceState, dataBinding.getRoot(), dataBinding);
        return dataBinding.getRoot();
    }

    protected abstract void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, T viewDataBinding);

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            IBinder token = requireActivity().findViewById(android.R.id.content).getWindowToken();
            imm.hideSoftInputFromWindow(token, 0);
        }
    }

    @SuppressWarnings("unchecked")
    protected T getDataBinding() {
        return dataBinding;
    }

    public NavController getNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @NonNull
    protected <U extends ViewModel> U getViewModel(@NonNull Class<U> modelClass) {
        return new ViewModelProvider(this, viewModelFactory).get(modelClass);
    }

    @LayoutRes
    protected abstract int getLayoutId();
}
