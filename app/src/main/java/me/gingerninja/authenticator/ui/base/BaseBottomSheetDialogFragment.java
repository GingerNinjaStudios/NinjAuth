package me.gingerninja.authenticator.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public abstract class BaseBottomSheetDialogFragment<T extends ViewDataBinding> extends BottomSheetDialogFragment {
    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    protected T dataBinding;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
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

    @NonNull
    protected <U extends ViewModel> U getViewModel(@NonNull Class<U> modelClass) {
        return ViewModelProviders.of(this, viewModelFactory).get(modelClass);
    }

    @LayoutRes
    protected abstract int getLayoutId();
}
