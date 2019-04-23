package me.gingerninja.authenticator.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.AccountListIteratorAdapter;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.databinding.AccountListFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.form.AccountEditorFragment;
import me.gingerninja.authenticator.ui.home.list.AccountListItemViewModel;
import timber.log.Timber;

public class AccountListFragment extends BaseFragment<AccountListFragmentBinding> implements BottomNavigationFragment.BottomNavigationListener, AccountListItemViewModel.AccountMenuItemClickListener {
    public static final String ACCOUNT_OP_ADD = "accountAdded";
    public static final String ACCOUNT_OP_UPDATE = "accountUpdated";
    public static final String ACCOUNT_OP_DELETE = "accountDeleted";
    private static final int REQUEST_CODE_ADD = 0x1000;
    private static final int REQUEST_CODE_EDIT = 0x1001;
    private static final String BOTTOM_LABELS_TAG = "bottomLabelsFrag";
    private static final String ADD_ACCOUNT_TAG = "newAccount";
    @Inject
    AccountListIteratorAdapter accountListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountListAdapter.setMenuItemClickListener(this);

        AccountListViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountListViewModel.class);

        viewModel.getAccountList2().observe(this, accountListAdapter::setResults);

        viewModel
                .getNavigationAction()
                .observe(this, rawEvent -> {
                    if (rawEvent.handle()) {
                        String eventId = rawEvent.getId();
                        switch (eventId) {
                            case AccountListViewModel.NAV_ADD_ACCOUNT_FROM_CAMERA:
                                showAddNewAccountMenu();
                                break;
                        }
                    }
                });
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountListFragmentBinding viewDataBinding) {
        subscribeToUi(viewDataBinding);
    }

    @Override
    public void onResume() {
        super.onResume();
        accountListAdapter.startClock();
    }

    @Override
    public void onPause() {
        super.onPause();
        accountListAdapter.stopClock();
    }

    private void subscribeToUi(AccountListFragmentBinding binding) {
        AccountListViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountListViewModel.class);
        binding.setViewModel(viewModel);

        /*viewModel
                .getNavigationAction()
                .observe(this, rawEvent -> {
                    if (rawEvent.handle()) {
                        String eventId = rawEvent.getId();
                        switch (eventId) {
                            case AccountListViewModel.NAV_ADD_ACCOUNT_FROM_CAMERA:
                                showAddNewAccountMenu();
                                break;
                        }
                    }
                });

        viewModel.getAccountList2().observe(this, accountListAdapter::setResults);*/

        binding.accountList.setAdapter(accountListAdapter);
        enableListDrag(binding);

        if (getArguments() != null) {
            AccountListFragmentArgs args = AccountListFragmentArgs.fromBundle(getArguments());

            if (args.getAccountOperation() != null) {
                switch (args.getAccountOperation()) {
                    case ACCOUNT_OP_ADD:
                        Snackbar.make(binding.accountList, "New account added: " + args.getAccountName(), Snackbar.LENGTH_LONG)
                                .setAnchorView(binding.fab)
                                .show();
                        getArguments().clear();
                        break;
                }
            }
        }

        binding.appBar.setNavigationOnClickListener(v -> {
            BottomNavigationFragment.show(R.menu.navigation_menu, R.id.nav_accounts, getChildFragmentManager());
        });
        binding.appBar.inflateMenu(R.menu.account_list_menu);
        binding.appBar.setOnMenuItemClickListener(item -> {
            return true;
        });

        //binding.toolbar.inflateMenu(R.menu.navigation_menu);
    }

    private void enableListDrag(AccountListFragmentBinding binding) {
        AccountListViewModel viewModel = binding.getViewModel();
        ItemTouchHelper dragHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                accountListAdapter.onItemDrag(viewHolder, false);
                viewModel.saveListOrder(accountListAdapter.getItemCount(), accountListAdapter.getMovementAndReset());
                Timber.v("clearView() - Drag finished");
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                Timber.v("onSelectedChanged() - actionState: %d", actionState);
                accountListAdapter.onItemDrag(viewHolder, actionState == ItemTouchHelper.ACTION_STATE_DRAG);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return accountListAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        dragHelper.attachToRecyclerView(binding.accountList);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.account_list_fragment;
    }

    private void showAddNewAccountMenu() {
        BottomNavigationFragment bottomNavFragment = BottomNavigationFragment.create(R.menu.add_account_menu, 0);
        bottomNavFragment.show(getChildFragmentManager(), ADD_ACCOUNT_TAG);
    }

    @Override
    public void onBottomNavigationSelected(@Nullable String tag, int id) {
        if (tag == null) {
            return;
        }

        switch (tag) {
            case ADD_ACCOUNT_TAG:
                handleAddAccountMenu(id);
                break;
            case BottomNavigationFragment.BOTTOM_NAV_TAG:
                handleMainMenu(id);
                break;
        }
    }

    private void handleAddAccountMenu(int id) {
        switch (id) {
            case R.id.menu_add_account_from_camera:
                //getNavController().navigate(R.id.addAccountFromCameraFragment);
                navigateForResult(REQUEST_CODE_ADD).navigate(R.id.addAccountFromCameraFragment);
                break;
            case R.id.menu_add_account_manual:
                //getNavController().navigate(R.id.accountEditorFragment);
                navigateForResult(REQUEST_CODE_ADD).navigate(R.id.accountEditorFragment);
                break;
        }
    }

    private void handleMainMenu(int id) {
        switch (id) {
            case R.id.nav_labels:
                getNavController().navigate(R.id.openLabelListFromAccountsAction);
                break;
            case R.id.nav_settings:
                getNavController().navigate(R.id.openSettingsFromHomeAction);
                break;
        }
    }

    @Override
    public void onAccountMenuItemClicked(MenuItem item, Account account) {
        switch (item.getItemId()) {
            case R.id.menu_account_edit:
                AccountListFragmentDirections.EditAccountAction action = AccountListFragmentDirections.editAccountAction().setId(account.getId());
                //getNavController().navigate(action);
                navigateForResult(REQUEST_CODE_EDIT).navigate(action);
                break;
            case R.id.menu_account_delete:
                DeleteAccountBottomFragment.show(account, getChildFragmentManager());
                break;
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Object data) {
        AccountListFragmentBinding binding = getDataBinding();

        if (resultCode == RESULT_OK && (requestCode == REQUEST_CODE_EDIT || requestCode == REQUEST_CODE_ADD)) {
            assert data != null;

            Intent intent = (Intent) data;

            String action = intent.getAction();
            if (action != null) {
                String accName = intent.getStringExtra(AccountEditorFragment.RESULT_ARG_ACCOUNT_NAME);
                switch (action) {
                    case ACCOUNT_OP_ADD:
                        Snackbar.make(binding.accountList, "New account added: " + accName, Snackbar.LENGTH_LONG)
                                .setAnchorView(binding.fab)
                                .show();
                        break;
                    case ACCOUNT_OP_UPDATE:
                        Snackbar.make(binding.accountList, "Saved account: " + accName, Snackbar.LENGTH_LONG)
                                .setAnchorView(binding.fab)
                                .show();
                        break;
                }
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == REQUEST_CODE_ADD) {
            Snackbar.make(binding.accountList, "Canceled adding a new account", Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.fab)
                    .show();
        }
    }
}
