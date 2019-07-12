package me.gingerninja.authenticator.ui.home.filter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.AccountFilterDialogFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseBottomSheetDialogFragment;
import me.gingerninja.authenticator.ui.home.AccountListViewModel;
import me.gingerninja.authenticator.util.SingleEvent;

public class AccountFilterDialogFragment extends BaseBottomSheetDialogFragment<AccountFilterDialogFragmentBinding> implements AccountFilterLabelAdapter.LabelFilterListener {

    @Inject
    AccountFilterLabelAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountListViewModel parentViewModel = ViewModelProviders.of(requireParentFragment(), viewModelFactory).get(AccountListViewModel.class);
        AccountFilterViewModel viewModel = getViewModel(AccountFilterViewModel.class);
        viewModel.setFilterObject(parentViewModel.getFilterObject());
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountFilterDialogFragmentBinding binding) {
        AccountFilterViewModel viewModel = getViewModel(AccountFilterViewModel.class);
        viewModel.getLabels().observe(getViewLifecycleOwner(), adapter::setLabels);
        viewModel.getEvents().observe(getViewLifecycleOwner(), this::handleEvents);
        binding.setViewModel(viewModel);

        adapter.setFilterListener(this);
        viewModel.getFilterLabels().observe(getViewLifecycleOwner(), adapter::setFilterLabels);

        binding.filterList.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.filterList.setAdapter(adapter);
        /*binding.filterList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //binding.appBarLayout.setLifted(recyclerView.computeVerticalScrollOffset() > 0);
                //Timber.v("Scroll: extent: %d, offset: %d, range: %d", recyclerView.computeVerticalScrollExtent(), recyclerView.computeVerticalScrollOffset(), recyclerView.computeVerticalScrollRange());
            }
        });*/

        binding.btnClose.setOnClickListener(this::closeDialog);
    }

    private void closeDialog(View view) {
        dismiss();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.account_filter_dialog_fragment;
    }

    @Override
    public void onLabelAdded(Label label) {
        AccountFilterViewModel viewModel = getViewModel(AccountFilterViewModel.class);
        viewModel.addLabelToFilter(label);
    }

    @Override
    public void onLabelRemoved(Label label) {
        AccountFilterViewModel viewModel = getViewModel(AccountFilterViewModel.class);
        viewModel.removeLabelFromFilter(label);
    }

    private void setFilterForParent() {
        AccountFilterViewModel viewModel = getViewModel(AccountFilterViewModel.class);
        AccountFilterObject filterObject = viewModel.getFilterObject();

        if (filterObject != null && !filterObject.hasLabels() && !filterObject.hasSearchString()) {
            filterObject = null;
        }

        AccountListViewModel parentViewModel = ViewModelProviders.of(requireParentFragment(), viewModelFactory).get(AccountListViewModel.class);
        parentViewModel.setFilterAndRetrieve(filterObject);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        setFilterForParent();
    }

    private void handleEvents(SingleEvent event) {
        if (event.handle()) {
            switch (event.getId()) {
                case AccountFilterViewModel.EVENT_FILTER_AND_DISMISS:
                    dismiss();
                    break;
            }
        }
    }
}
