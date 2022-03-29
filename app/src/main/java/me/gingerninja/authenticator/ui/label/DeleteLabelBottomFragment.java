package me.gingerninja.authenticator.ui.label;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.DeleteAccountFragmentBinding;
import me.gingerninja.authenticator.databinding.DeleteLabelFragmentBinding;
import me.gingerninja.authenticator.ui.home.DeleteAccountViewModel;

public class DeleteLabelBottomFragment extends BottomSheetDialogFragment {
    private static final String FRAGMENT_MANAGER_TAG = "label.delete";
    static final String ARG_LABEL_ID = "labelId";
    static final String ARG_LABEL_NAME = "labelName";

    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    public static void show(@NonNull Label label, @NonNull FragmentManager fragmentManager) {
        Bundle args = new Bundle();
        args.putLong(ARG_LABEL_ID, label.getId());
        args.putString(ARG_LABEL_NAME, label.getName());

        DeleteLabelBottomFragment fragment = new DeleteLabelBottomFragment();
        fragment.setArguments(args);
        fragment.show(fragmentManager, FRAGMENT_MANAGER_TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DeleteLabelFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.delete_label_fragment, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        assert args != null;

        DeleteLabelViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(DeleteLabelViewModel.class);
        viewModel.setData(args);
        binding.setViewModel(viewModel);

        viewModel
                .getAction()
                .observe(getViewLifecycleOwner(), event -> {
                    switch (event) {
                        case DeleteLabelViewModel.ACTION_CANCEL:
                            dismiss();
                            break;
                        case DeleteLabelViewModel.ACTION_DELETE:
                            // TODO
                            dismiss();
                            break;
                    }
                });

        return root;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

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
}
