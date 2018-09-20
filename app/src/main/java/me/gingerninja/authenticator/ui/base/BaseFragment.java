package me.gingerninja.authenticator.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.DaggerFragment;
import me.gingerninja.authenticator.R;

public abstract class BaseFragment<T extends ViewDataBinding> extends DaggerFragment {
    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    protected T dataBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        onCreateView(inflater, container, savedInstanceState, dataBinding.getRoot(), dataBinding);
        return dataBinding.getRoot();
    }

    protected abstract void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, T viewDataBinding);

    @SuppressWarnings("unchecked")
    protected T getDataBinding() {
        return dataBinding;
    }

    protected NavController getNavController() {
        return Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
    }

    @LayoutRes
    protected abstract int getLayoutId();
}
