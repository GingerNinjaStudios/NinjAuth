package me.gingerninja.authenticator.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.AccountListAdapter;
import me.gingerninja.authenticator.databinding.AccountListFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class AccountListFragment extends BaseFragment<AccountListFragmentBinding> {
    private static final String TAG = "AccountListFragment";

    @Inject
    AccountListAdapter accountListAdapter;

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountListFragmentBinding viewDataBinding) {
        subscribeToUi(viewDataBinding);
    }

    private void subscribeToUi(AccountListFragmentBinding binding) {
        AccountListViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountListViewModel.class);
        binding.setViewModel(viewModel);

        viewModel
                .getNavigationAction()
                .observe(this, rawEvent -> {
                    String event = rawEvent.getContentAndMarkHandled();
                    if (event != null) {
                        switch (event) {
                            case AccountListViewModel.NAV_ADD_ACCOUNT_FROM_CAMERA:
                                getNavController().navigate(R.id.addAccountFromCameraFragment);
                                break;
                        }
                    }
                });

        viewModel.getAccountList().observe(this, accountListAdapter::setAccountList);

        binding.accountList.setAdapter(accountListAdapter);

        binding.appBar.setNavigationOnClickListener(v -> {
            BottomNavigationFragment bottomNavFragment = new BottomNavigationFragment();
            bottomNavFragment.show(getChildFragmentManager(), "bottomNavFrag");
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.account_list_fragment;
    }
}
