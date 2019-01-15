package me.gingerninja.authenticator.ui.label.form;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.takisoft.colorpicker.ColorPickerDialog;
import com.takisoft.colorpicker.ColorPickerDialogFragment;
import com.takisoft.colorpicker.OnColorSelectedListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.LabelFormFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class LabelEditorFragment extends BaseFragment<LabelFormFragmentBinding> implements OnColorSelectedListener {
    private LabelEditorViewModel viewModel;

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, LabelFormFragmentBinding binding) {
        setupUi(binding, getArguments());
    }

    private void setupUi(LabelFormFragmentBinding binding, Bundle args) {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LabelEditorViewModel.class);
        viewModel.init(args);
        binding.setViewModel(viewModel);

        binding.toolbar.setNavigationOnClickListener(v -> {
            @SuppressWarnings("ConstantConditions")
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            getNavController().navigateUp();
        });

        viewModel.getNavigationAction().observe(this, event -> {
            if (event.handle()) {
                String eventId = event.getId();
                switch (eventId) {
                    case LabelEditorViewModel.NAV_ACTION_SAVE:
                        /*AddAccountFragmentDirections.SaveNewAccountAction action = AddAccountFragmentDirections.saveNewAccountAction()
                                .setAccountName(event.getContent())
                                .setAccountOperation(AccountListFragment.ACCOUNT_OP_ADD);
                        getNavController().navigate(action);*/
                        break;
                    case LabelEditorViewModel.NAV_ACTION_PICK_COLOR:
                        ColorPickerDialog.Params params = new ColorPickerDialog.Params.Builder(getContext())
                                .setSelectedColor(viewModel.getColor())
                                .build();
                        ColorPickerDialogFragment.newInstance(params).show(getChildFragmentManager(), "colorpicker");
                        break;
                }
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.label_form_fragment;
    }

    @Override
    public void onColorSelected(int color) {
        viewModel.setColor(color);
    }
}
