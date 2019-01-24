package me.gingerninja.authenticator.ui.account.form;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.AccountLabelListAdapter;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.AccountFormFragmentBinding;
import me.gingerninja.authenticator.ui.account.BaseEditableAccountViewModel;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.AccountListFragment;
import timber.log.Timber;

public class EditAccountFragment extends BaseFragment<AccountFormFragmentBinding> implements LabelClickListener, LabelListClickListener {

    private AccountLabelListAdapter labelListAdapter;

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountFormFragmentBinding binding) {
        setupUi(binding, getArguments());
    }

    private void setupUi(AccountFormFragmentBinding binding, Bundle args) {
        EditAccountViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditAccountViewModel.class);
        viewModel.init(args);
        binding.setViewModel(viewModel);

        binding.toolbar.setNavigationOnClickListener(v -> {
            @SuppressWarnings("ConstantConditions")
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            getNavController().navigateUp();
        });

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
        BaseEditableAccountViewModel viewModel = binding.getViewModel();
        ItemTouchHelper dragHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END | ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

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
    public void onLabelRemoved(Label label) {
        Snackbar.make(getView(), "Removed label: " + label.getName(), 5000)
                .setAction("Undo", v -> {
                    // TODO
                })
                .show();
    }

    @Override
    public void onNewLabelClicked(View view) {

    }

    @Override
    public void onLabelSelected(Label label) {
        labelListAdapter.addLabel(label);
    }
}
