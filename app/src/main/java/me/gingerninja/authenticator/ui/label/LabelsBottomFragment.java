package me.gingerninja.authenticator.ui.label;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.LabelListIteratorAdapter;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.LabelsFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.BottomNavigationFragment;
import me.gingerninja.authenticator.ui.label.form.LabelEditorFragment;
import me.gingerninja.authenticator.util.RequestCodes;
import timber.log.Timber;

public class LabelsBottomFragment extends BaseFragment<LabelsFragmentBinding> implements BottomNavigationFragment.BottomNavigationListener, LabelListItemViewModel.LabelMenuItemClickListener {
    public static final String LABEL_OP_ADD = "labelAdded";
    public static final String LABEL_OP_UPDATE = "labelUpdated";
    public static final String LABEL_OP_DELETE = "labelDeleted";

    @Inject
    LabelListIteratorAdapter labelListAdapter;

    private final OnBackPressedCallback backButtonCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            LabelsViewModel viewModel = getViewModel(LabelsViewModel.class);
            if (viewModel.isOrdering.get()) {
                viewModel.setReorderingEnabled(false);
            } else {
                getNavController().popBackStack();
            }
        }
    };

    private ItemTouchHelper dragHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            labelListAdapter.onItemDrag(viewHolder, false);
            getViewModel(LabelsViewModel.class).saveListOrder(labelListAdapter.getItemCount(), labelListAdapter.getMovementAndReset());
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
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        labelListAdapter.setMenuItemClickListener(this);

        LabelsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(LabelsViewModel.class);

        viewModel
                .getNavigationAction()
                .observe(this, rawEvent -> {
                    if (rawEvent.handle()) {
                        String eventId = rawEvent.getId();
                        switch (eventId) {
                            case LabelsViewModel.NAV_ADD_LABEL:
                                //getNavController().navigate(R.id.labelEditorFragment);
                                navigateForResult(RequestCodes.LABEL_ADD).navigate(R.id.labelEditorFragment);
                                break;
                        }
                    }
                });

        viewModel.getLabelList().observe(this, labelListAdapter::setResults);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backButtonCallback);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, LabelsFragmentBinding viewDataBinding) {
        subscribeToUi(getDataBinding());
    }

    private void subscribeToUi(LabelsFragmentBinding binding) {
        LabelsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(LabelsViewModel.class);
        binding.setViewModel(viewModel);

        binding.labelList.setAdapter(labelListAdapter);

        enableListDrag(binding);

        binding.appBar.setNavigationOnClickListener(v -> {
            BottomNavigationFragment.show(R.menu.navigation_menu, R.id.nav_labels, R.layout.bottom_nav_header, getChildFragmentManager());
        });

        binding.appBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                /*case R.id.menu_filter:
                    break;*/
                case R.id.menu_order:
                    viewModel.setReorderingEnabled(true);
                    Snackbar.make(binding.labelList, R.string.reorder_tutorial_msg, Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.fab)
                            .show();
                    break;
            }
            return true;
        });
    }

    private void enableListDrag(LabelsFragmentBinding binding) {
        LabelsViewModel viewModel = binding.getViewModel();
        viewModel.getIsOrdering().observe(getViewLifecycleOwner(), isOrdering -> {
            dragHelper.attachToRecyclerView(isOrdering ? binding.labelList : null);
            labelListAdapter.setDragEnabled(isOrdering);
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.labels_fragment;
    }

    @Override
    public void onBottomNavigationSelected(@Nullable String tag, int id) {
        if (tag == null) {
            return;
        }

        getViewModel(LabelsViewModel.class).setReorderingEnabled(false);

        switch (tag) {
            case BottomNavigationFragment.BOTTOM_NAV_TAG:
                handleMainMenu(id);
                break;
        }
    }

    private void handleMainMenu(int id) {
        switch (id) {
            case R.id.nav_accounts:
                getNavController().popBackStack(R.id.accountListFragment, false);
                //getNavController().navigate(R.id.openAccountListFromLabelsAction);
                break;
            case R.id.nav_settings:
                getNavController().navigate(R.id.openSettingsFromLabelsAction);
                break;
        }
    }

    @Override
    public void onLabelMenuItemClicked(MenuItem item, Label label) {
        switch (item.getItemId()) {
            case R.id.menu_account_edit:
                LabelsBottomFragmentDirections.EditLabelAction action = LabelsBottomFragmentDirections.editLabelAction().setId(label.getId());
                //AccountListFragmentDirections.EditAccountAction action = AccountListFragmentDirections.editAccountAction(account.getId());
                //getNavController().navigate(action);
                navigateForResult(RequestCodes.LABEL_EDIT).navigate(action);
                break;
            case R.id.menu_account_delete:
                DeleteLabelBottomFragment.show(label, getChildFragmentManager());
                break;
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Object data) {
        switch (requestCode) {
            case RequestCodes.LABEL_ADD:
                break;
            case RequestCodes.LABEL_EDIT:
                if (resultCode == RESULT_OK && data != null) {
                    LabelEditorFragment.LabelResult result = (LabelEditorFragment.LabelResult) data;

                }
                break;
        }
    }

    /*@Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        return dialog;
    }*/
}
