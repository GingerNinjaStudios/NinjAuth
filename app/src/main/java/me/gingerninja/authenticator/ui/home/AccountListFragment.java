package me.gingerninja.authenticator.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.AccountListAdapter;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.databinding.AccountListFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.list.AccountListItemViewModel;
import timber.log.Timber;

public class AccountListFragment extends BaseFragment<AccountListFragmentBinding> implements BottomNavigationFragment.BottomNavigationListener, AccountListItemViewModel.AccountMenuItemClickListener {
    private static final String TAG = "AccountListFragment";
    private static final String BOTTOM_NAV_TAG = "bottomNavFrag";
    private static final String ADD_ACCOUNT_TAG = "newAccount";

    public static final String ACCOUNT_OP_ADD = "accountAdded";
    public static final String ACCOUNT_OP_UPDATE = "accountUpdated";
    public static final String ACCOUNT_OP_DELETE = "accountDeleted";

    @Inject
    AccountListAdapter accountListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountListAdapter.setMenuItemClickListener(this);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountListFragmentBinding viewDataBinding) {
        subscribeToUi(viewDataBinding);

        accountListAdapter.startClock();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        accountListAdapter.stopClock();
    }

    private void subscribeToUi(AccountListFragmentBinding binding) {
        AccountListViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(AccountListViewModel.class);
        binding.setViewModel(viewModel);

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

        viewModel.getAccountList().observe(this, accountListAdapter::setAccountList);

        binding.accountList.setAdapter(accountListAdapter);
        enableListDrag(binding);

        if (getArguments() != null) {
            AccountListFragmentArgs args = AccountListFragmentArgs.fromBundle(getArguments());

            if (args.getAccountOperation() != null) {
                switch (args.getAccountOperation()) {
                    case ACCOUNT_OP_ADD:
                        Snackbar.make(binding.accountList, "New account added: " + args.getAccountName(), Snackbar.LENGTH_LONG).show();
                        break;
                }
            }
        }

        binding.appBar.setNavigationOnClickListener(v -> {
            BottomNavigationFragment bottomNavFragment = BottomNavigationFragment.create(R.menu.navigation_menu);
            bottomNavFragment.show(getChildFragmentManager(), BOTTOM_NAV_TAG);
        });
    }

    private void enableListDrag(AccountListFragmentBinding binding) {
        AccountListViewModel viewModel = binding.getViewModel();
        ItemTouchHelper dragHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                accountListAdapter.onItemDrag(viewHolder, false);
                viewModel.saveList(accountListAdapter.getAccountList());
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
        BottomNavigationFragment bottomNavFragment = BottomNavigationFragment.create(R.menu.add_account_menu);
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
            case BOTTOM_NAV_TAG:
                handleMainMenu(id);
                break;
        }
    }

    private void handleAddAccountMenu(int id) {
        switch (id) {
            case R.id.menu_add_account_from_camera:
                getNavController().navigate(R.id.addAccountFromCameraFragment);
                break;
            case R.id.menu_add_account_manual:
                getNavController().navigate(R.id.addAccountFragment);
                break;
        }
    }

    private void handleMainMenu(int id) {
        switch (id) {
            case R.id.nav_settings:
                getNavController().navigate(R.id.openSettingsFromHomeAction);
                break;
        }
    }

    @Override
    public void onAccountMenuItemClicked(MenuItem item, Account account) {
        switch (item.getItemId()) {
            case R.id.menu_account_edit:
                AccountListFragmentDirections.EditAccountAction action = AccountListFragmentDirections.editAccountAction(account.getId());
                getNavController().navigate(action);
                break;
            case R.id.menu_account_delete:
                Snackbar.make(getView(), "Delete: " + account, Snackbar.LENGTH_SHORT).show();
                break;
        }
    }
}
