package me.gingerninja.authenticator.ui.label.form;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.takisoft.colorpicker.ColorPickerDialog;
import com.takisoft.colorpicker.ColorPickerDialogFragment;
import com.takisoft.colorpicker.OnColorSelectedListener;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
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
        viewModel.setMode(LabelEditorFragmentArgs.fromBundle(args).getId() == 0 ? LabelEditorViewModel.MODE_CREATE : LabelEditorViewModel.MODE_EDIT);
        binding.setViewModel(viewModel);

        binding.toolbar.setNavigationOnClickListener(v -> {
            @SuppressWarnings("ConstantConditions")
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            getNavController().navigateUp();
        });

        viewModel.getNavigationAction().observe(getViewLifecycleOwner(), event -> {
            if (event.handle()) {
                String eventId = event.getId();
                switch (eventId) {
                    case LabelEditorViewModel.NAV_ACTION_SAVE:
                        /*LabelEditorFragmentDirections.SaveLabelAction action = LabelEditorFragmentDirections.saveLabelAction()
                                .setLabelName(event.getContent())
                                .setOperation(viewModel.getMode() == LabelEditorViewModel.MODE_CREATE ? LabelsBottomFragment.LABEL_OP_ADD : LabelsBottomFragment.LABEL_OP_UPDATE);
                        getNavController().navigate(action);*/

                        setResultAndLeave(RESULT_OK, new LabelResult(event.getContent(), viewModel.getMode()));
                        //LabelEditorFragmentDirections.saveLabelAction()
                        /*AddAccountFragmentDirections.SaveNewAccountAction action = AddAccountFragmentDirections.saveNewAccountAction()
                                .setAccountName(event.getContent())
                                .setAccountOperation(AccountListFragment.ACCOUNT_OP_ADD);
                        getNavController().navigate(action);*/
                        break;
                    case LabelEditorViewModel.NAV_ACTION_PICK_COLOR:
                        ColorPickerDialog.Params params = new ColorPickerDialog.Params.Builder(getContext())
                                .setSelectedColor(viewModel.getColor())
                                .build();
                        ColorPickerDialogFragment
                                .newInstance(params)
                                .show(getChildFragmentManager(), "colorpicker");
                        break;
                    case LabelEditorViewModel.NAV_ACTION_PICK_ICON:
                        LabelIconPickerDialogFragment
                                .newInstance(viewModel.getIcon())
                                .show(getChildFragmentManager(), "iconpicker");
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

    public static class LabelResult {
        private final Label label;

        @LabelEditorViewModel.Mode
        private final int mode;

        private LabelResult(Label label, @LabelEditorViewModel.Mode int mode) {
            this.label = label;
            this.mode = mode;
        }

        public Label getLabel() {
            return label;
        }

        @LabelEditorViewModel.Mode
        public int getMode() {
            return mode;
        }
    }
}
