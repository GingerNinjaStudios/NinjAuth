package me.gingerninja.authenticator.ui.label;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.LabelListAdapter;
import me.gingerninja.authenticator.databinding.LabelsFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.BottomNavigationFragment;

public class LabelsBottomFragment extends BaseFragment<LabelsFragmentBinding> implements BottomNavigationFragment.BottomNavigationListener {
    @Inject
    LabelListAdapter labelListAdapter;

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, LabelsFragmentBinding viewDataBinding) {
        subscribeToUi(getDataBinding());
    }

    private void subscribeToUi(LabelsFragmentBinding binding) {
        LabelsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(LabelsViewModel.class);
        binding.setViewModel(viewModel);

        viewModel
                .getNavigationAction()
                .observe(this, rawEvent -> {
                    if (rawEvent.handle()) {
                        String eventId = rawEvent.getId();
                        switch (eventId) {
                            case LabelsViewModel.NAV_ADD_LABEL:
                                // TODO showAddNewLabel();
                                break;
                        }
                    }
                });

        viewModel.getLabelList().observe(this, labelListAdapter::setLabelList);

        binding.labelList.setAdapter(labelListAdapter);

        binding.appBar.setNavigationOnClickListener(v -> {
            BottomNavigationFragment.show(R.menu.navigation_menu, getChildFragmentManager());
        });
        binding.appBar.inflateMenu(R.menu.account_list_menu);
        binding.appBar.setOnMenuItemClickListener(item -> {
            return true;
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
                getNavController().navigate(R.id.openSettingsFromHomeAction);
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
