package me.gingerninja.authenticator.ui.home.filter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.AccountFilterDialogFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseBottomSheetDialogFragment;

public class AccountFilterDialogFragment extends BaseBottomSheetDialogFragment<AccountFilterDialogFragmentBinding> implements AccountFilterLabelAdapter.LabelFilterListener {

    @Inject
    AccountFilterLabelAdapter adapter;

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountFilterDialogFragmentBinding binding) {
        AccountFilterViewModel viewModel = getViewModel(AccountFilterViewModel.class);
        viewModel.getLabels().observe(getViewLifecycleOwner(), adapter::setLabels);
        binding.setViewModel(viewModel);

        adapter.setFilterListener(this);
        viewModel.getFilterLabels().observe(getViewLifecycleOwner(), adapter::setFilterLabels);

        binding.filterList.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.filterList.setAdapter(adapter);
        binding.filterList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //binding.appBarLayout.setLifted(recyclerView.computeVerticalScrollOffset() > 0);
                //Timber.v("Scroll: extent: %d, offset: %d, range: %d", recyclerView.computeVerticalScrollExtent(), recyclerView.computeVerticalScrollOffset(), recyclerView.computeVerticalScrollRange());
            }
        });

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
}
