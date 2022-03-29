package me.gingerninja.authenticator.ui.home.form;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.AccountLabelListAdapter;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.AccountFormFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.AccountListFragment;
import timber.log.Timber;

public class AccountEditorFragment extends BaseFragment<AccountFormFragmentBinding> implements LabelClickListener, LabelListClickListener, ExistingAccountDialogFragment.ExistingAccountActionListener {
    public static final String RESULT_ARG_ACCOUNT_NAME = "accountName";
    private AccountLabelListAdapter labelListAdapter;

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountFormFragmentBinding binding) {
        setupUi(binding, getArguments());
    }

    private void setupUi(AccountFormFragmentBinding binding, Bundle args) {
        AccountEditorViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(AccountEditorViewModel.class);
        viewModel.init(args);
        viewModel.setMode(AccountEditorFragmentArgs.fromBundle(args).getId() == 0 ? AccountEditorViewModel.MODE_CREATE : AccountEditorViewModel.MODE_EDIT);
        binding.setViewModel(viewModel);

        binding.toolbar.setNavigationOnClickListener(v -> {
            @SuppressWarnings("ConstantConditions")
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            getNavController().navigateUp();
        });

        viewModel.getNavigationAction().observe(getViewLifecycleOwner(), event -> {
            if (event.handle()) {
                String eventId = event.getId();
                switch (eventId) {
                    case AccountEditorViewModel.NAV_ACTION_SAVE:
                        /*AccountEditorFragmentDirections.SaveAccountAction action = AccountEditorFragmentDirections.saveAccountAction()
                                .setAccountName(event.getContent())
                                .setAccountOperation(AccountListFragment.ACCOUNT_OP_UPDATE);
                        getNavController().navigate(action);*/
                        Intent i = new Intent(viewModel.getMode() == AccountEditorViewModel.MODE_CREATE ? AccountListFragment.ACCOUNT_OP_ADD : AccountListFragment.ACCOUNT_OP_UPDATE).putExtra(RESULT_ARG_ACCOUNT_NAME, (String) event.getContent());
                        setResultAndLeave(Activity.RESULT_OK, i, R.id.accountListFragment, false);
                        break;
                }
            }
        });

        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            if (event.handle()) {
                switch (event.getId()) {
                    case AccountEditorViewModel.EVENT_EXISTING_ACCOUNT:
                        ExistingAccountDialogFragment dialog = new ExistingAccountDialogFragment();
                        dialog.show(getChildFragmentManager(), ExistingAccountDialogFragment.TAG);
                        break;
                }
            }
        });

        // labels
        labelListAdapter = new AccountLabelListAdapter(viewModel.getLabels()).setLabelClickListener(this);
        /*FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);*/
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.labelList.setLayoutManager(layoutManager);
        binding.labelList.setItemAnimator(new DefaultItemAnimator());
        binding.labelList.setAdapter(labelListAdapter);

        enableListDrag(binding);
    }

    private void enableListDrag(@NonNull AccountFormFragmentBinding binding) {
        AccountEditorViewModel viewModel = binding.getViewModel();
        ItemTouchHelper dragHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END, 0) {

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                labelListAdapter.onItemDrag(viewHolder, false);
                Timber.v("clearView() - Drag finished");
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                Timber.v("onSelectedChanged() - actionState: %d", actionState);
                labelListAdapter.onItemDrag(viewHolder, actionState == ItemTouchHelper.ACTION_STATE_DRAG);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return labelListAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public int getDragDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int pos = viewHolder.getAdapterPosition();
                int count = recyclerView.getAdapter().getItemCount();
                if (pos >= count - 1) {
                    return 0;
                }
                return super.getDragDirs(recyclerView, viewHolder);
            }
        });
        dragHelper.attachToRecyclerView(binding.labelList);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.account_form_fragment;
    }

    @Override
    public void onLabelAddClicked(View view) {
        LabelSelectorDialogFragment.show(getChildFragmentManager(), labelListAdapter.getLabels());
    }

    @Override
    public void onLabelRemoved(Label label, int position) {
        Snackbar.make(getView(), getString(R.string.account_form_removed_label, label.getName()), 5000)
                .setAction(R.string.undo, v -> labelListAdapter.addLabel(label, position))
                .show();
    }

    @Override
    public void onLabelSelected(Label label) {
        labelListAdapter.addLabel(label);
    }

    @Override
    public void onCancelExistingAccount() {
        setResultAndLeave(RESULT_CANCELED);
    }
}
