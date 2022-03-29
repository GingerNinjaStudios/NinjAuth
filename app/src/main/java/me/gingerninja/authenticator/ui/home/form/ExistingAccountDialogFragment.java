package me.gingerninja.authenticator.ui.home.form;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.ExistingAccountDialogFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseBottomSheetDialogFragment;

public class ExistingAccountDialogFragment extends BaseBottomSheetDialogFragment<ExistingAccountDialogFragmentBinding> {
    static final String TAG = "existingAccountDialog";

    private ExistingAccountActionListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof ExistingAccountActionListener) {
            listener = (ExistingAccountActionListener) context;
        } else if (getParentFragment() instanceof ExistingAccountActionListener) {
            listener = (ExistingAccountActionListener) getParentFragment();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ExistingAccountViewModel viewModel = getViewModel(ExistingAccountViewModel.class);
        AccountEditorViewModel parentViewModel = new ViewModelProvider(requireParentFragment()).get(AccountEditorViewModel.class);
        //noinspection ConstantConditions
        viewModel.setAccounts(parentViewModel.getExistingAccount(), parentViewModel.getNewAccount());
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, ExistingAccountDialogFragmentBinding binding) {
        binding.btnCancel.setOnClickListener(view -> {
            if (listener != null) {
                listener.onCancelExistingAccount();
            }
            dismiss();
        });
        binding.btnEdit.setOnClickListener(view -> dismiss());

        ExistingAccountViewModel viewModel = getViewModel(ExistingAccountViewModel.class);
        binding.setViewModel(viewModel);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        setCancelable(false);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.existing_account_dialog_fragment;
    }

    public interface ExistingAccountActionListener {
        void onCancelExistingAccount();
    }
}
