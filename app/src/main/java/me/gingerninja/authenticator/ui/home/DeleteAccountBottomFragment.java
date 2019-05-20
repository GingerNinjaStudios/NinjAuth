package me.gingerninja.authenticator.ui.home;

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
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.databinding.DeleteAccountFragmentBinding;

public class DeleteAccountBottomFragment extends BottomSheetDialogFragment {
    public static final String FRAGMENT_MANAGER_TAG = "account.delete";
    static final String ARG_ACCOUNT_ID = "accountId";
    static final String ARG_ACCOUNT_TITLE = "accountTitle";
    static final String ARG_ACCOUNT_NAME = "accountName";
    static final String ARG_ACCOUNT_ISSUER = "accountIssuer";

    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    public static void show(@NonNull Account account, @NonNull FragmentManager fragmentManager) {
        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, account.getId());
        args.putString(ARG_ACCOUNT_TITLE, account.getTitle());
        args.putString(ARG_ACCOUNT_NAME, account.getAccountName());
        args.putString(ARG_ACCOUNT_ISSUER, account.getIssuer());

        DeleteAccountBottomFragment fragment = new DeleteAccountBottomFragment();
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
        DeleteAccountFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.delete_account_fragment, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        assert args != null;

        DeleteAccountViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(DeleteAccountViewModel.class);
        viewModel.setData(args);
        binding.setViewModel(viewModel);

        viewModel
                .getAction()
                .observe(this, event -> {
                    switch (event) {
                        case DeleteAccountViewModel.ACTION_CANCEL:
                            dismiss();
                            break;
                        case DeleteAccountViewModel.ACTION_DELETE:
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
