package me.gingerninja.authenticator.ui.setup;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.SkipSetupDialogFragmentBinding;

public class SkipConfirmationBottomFragment extends BottomSheetDialogFragment {
    public static final String FRAGMENT_MANAGER_TAG = "setup.skip.confirmation";

    private SkipDialogListener skipListener;

    public static void show(@NonNull FragmentManager fragmentManager) {
        SkipConfirmationBottomFragment fragment = new SkipConfirmationBottomFragment();
        fragment.show(fragmentManager, FRAGMENT_MANAGER_TAG);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getTargetFragment() instanceof SkipDialogListener) {
            skipListener = (SkipDialogListener) getTargetFragment();
        } else if (getParentFragment() instanceof SkipDialogListener) {
            skipListener = (SkipDialogListener) getParentFragment();
        } else if (context instanceof SkipDialogListener) {
            skipListener = (SkipDialogListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        skipListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SkipSetupDialogFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.skip_setup_dialog_fragment, container, false);
        View root = binding.getRoot();

        binding.btnCancel.setOnClickListener(v -> dismissAllowingStateLoss());
        binding.btnSkip.setOnClickListener(this::onSkipClick);

        return root;
    }

    private void onSkipClick(View v) {
        if (skipListener != null) {
            skipListener.onSkipSetup();
        }

        dismissAllowingStateLoss();
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

    public interface SkipDialogListener {
        void onSkipSetup();
    }
}
