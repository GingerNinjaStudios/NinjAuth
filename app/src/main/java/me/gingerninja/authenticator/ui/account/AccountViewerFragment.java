package me.gingerninja.authenticator.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.AccountViewerFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class AccountViewerFragment extends BaseFragment<AccountViewerFragmentBinding> {
    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountViewerFragmentBinding binding) {
        binding.toolbar.setNavigationOnClickListener(view -> getNavController().navigateUp());

        AccountViewerViewModel viewModel = getViewModel(AccountViewerViewModel.class);
        viewModel.init(requireArguments());
        binding.setViewModel(viewModel);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.account_viewer_fragment;
    }
}
