package me.gingerninja.authenticator.ui.account.form;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.AccountFormFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.AccountListFragment;

public class EditAccountFragment extends BaseFragment<AccountFormFragmentBinding> {

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountFormFragmentBinding binding) {
        setupUi(binding, getArguments());
    }

    private void setupUi(AccountFormFragmentBinding binding, Bundle args) {
        EditAccountViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditAccountViewModel.class);
        viewModel.init(args);
        binding.setViewModel(viewModel);

        binding.toolbar.setNavigationOnClickListener(v -> getNavController().navigateUp());

        viewModel.getNavigationAction().observe(this, event -> {
            if (event.handle()) {
                String eventId = event.getId();
                switch (eventId) {
                    case AddAccountViewModel.NAV_ACTION_SAVE:
                        EditAccountFragmentDirections.SaveExistingAccountAction action = EditAccountFragmentDirections.saveExistingAccountAction()
                                .setAccountName(event.getContent())
                                .setAccountOperation(AccountListFragment.ACCOUNT_OP_UPDATE);
                        getNavController().navigate(action);
                        break;
                }
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.account_form_fragment;
    }
}
