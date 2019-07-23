package me.gingerninja.authenticator.ui.home.form;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.ExistingAccountDialogFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseBottomSheetDialogFragment;

public class ExistingAccountDialogFragment extends BaseBottomSheetDialogFragment<ExistingAccountDialogFragmentBinding> {
    static final String TAG = "existingAccountDialog";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ExistingAccountViewModel viewModel = getViewModel(ExistingAccountViewModel.class);
        AccountEditorViewModel parentViewModel = ViewModelProviders.of(requireParentFragment()).get(AccountEditorViewModel.class);
        viewModel.setAccounts(parentViewModel.getExistingAccount(), parentViewModel.getNewAccount());
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, ExistingAccountDialogFragmentBinding binding) {
        binding.btnCancel.setOnClickListener(view -> dismiss());
        binding.btnEdit.setOnClickListener(view -> dismiss());

        ExistingAccountViewModel viewModel = getViewModel(ExistingAccountViewModel.class);
        binding.setViewModel(viewModel);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        setCancelable(false);

        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.existing_account_dialog_fragment;
    }
}
