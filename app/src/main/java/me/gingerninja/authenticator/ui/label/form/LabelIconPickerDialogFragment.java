package me.gingerninja.authenticator.ui.label.form;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.LabelIconSelectorDialogFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseDialogFragment;

public class LabelIconPickerDialogFragment extends BaseDialogFragment<LabelIconSelectorDialogFragmentBinding> implements LabelIconAdapter.IconListener {
    @SuppressWarnings("WeakerAccess")
    public static final String ARG_CURRENT_ICON = "icon";

    @Inject
    LabelIconAdapter adapter;

    static LabelIconPickerDialogFragment newInstance(@Nullable String icon) {
        Bundle args = new Bundle();
        args.putString(ARG_CURRENT_ICON, icon);

        LabelIconPickerDialogFragment fragment = new LabelIconPickerDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, LabelIconSelectorDialogFragmentBinding binding) {
        adapter.setSelectedIcon(requireArguments().getString(ARG_CURRENT_ICON));
        adapter.setIconListener(this);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(requireContext(), FlexDirection.ROW, FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.CENTER);

        binding.list.setLayoutManager(/*new GridLayoutManager(requireContext(), 4, GridLayoutManager.VERTICAL, false)*/layoutManager);
        binding.list.setAdapter(adapter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.label_icon_selector_dialog_fragment;
    }

    @Override
    protected void onCreateDialog(@NonNull MaterialAlertDialogBuilder builder) {
        builder.setNegativeButton(R.string.btn_remove, (dialogInterface, i) -> onIconSelected(null));
    }

    @Override
    public void onIconSelected(String icon) {
        LabelEditorViewModel parentViewModel = new ViewModelProvider(requireParentFragment()).get(LabelEditorViewModel.class);
        parentViewModel.setIcon(icon);
        dismiss();
    }
}
