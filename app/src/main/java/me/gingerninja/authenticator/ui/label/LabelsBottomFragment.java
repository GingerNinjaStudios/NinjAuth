package me.gingerninja.authenticator.ui.label;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.LabelListAdapter;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.LabelsFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.ui.home.BottomNavigationFragment;
import me.gingerninja.authenticator.ui.label.form.LabelEditorFragment;

public class LabelsBottomFragment extends BaseFragment<LabelsFragmentBinding> implements BottomNavigationFragment.BottomNavigationListener, LabelListItemViewModel.LabelMenuItemClickListener {
    public static final String LABEL_OP_ADD = "labelAdded";
    public static final String LABEL_OP_UPDATE = "labelUpdated";
    public static final String LABEL_OP_DELETE = "labelDeleted";
    private static final int REQUEST_CODE_ADD = 0x2000;
    private static final int REQUEST_CODE_EDIT = 0x2001;
    @Inject
    LabelListAdapter labelListAdapter;

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
                                navigateForResult(REQUEST_CODE_ADD).navigate(R.id.labelEditorFragment);
                                break;
                        }
                    }
                });

        viewModel.getLabelList().observe(this, labelListAdapter::setLabelList);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, LabelsFragmentBinding viewDataBinding) {
        subscribeToUi(getDataBinding());
    }

    private void subscribeToUi(LabelsFragmentBinding binding) {
        LabelsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(LabelsViewModel.class);
        binding.setViewModel(viewModel);

        binding.labelList.setAdapter(labelListAdapter);

        binding.appBar.setNavigationOnClickListener(v -> {
            BottomNavigationFragment.show(R.menu.navigation_menu, R.id.nav_labels, R.layout.bottom_nav_header, getChildFragmentManager());
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
                navigateForResult(REQUEST_CODE_EDIT).navigate(action);
                break;
            case R.id.menu_account_delete:
                //DeleteAccountBottomFragment.show(account, getChildFragmentManager());
                break;
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Object data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD:
                break;
            case REQUEST_CODE_EDIT:
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
