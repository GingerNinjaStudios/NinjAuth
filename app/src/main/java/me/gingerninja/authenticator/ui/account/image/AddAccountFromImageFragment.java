package me.gingerninja.authenticator.ui.account.image;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.AccountFromImageFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;


public class AddAccountFromImageFragment extends BaseFragment<AccountFromImageFragmentBinding> {
    private static final String STATE_OPENED_PICKER = "account.image.state.auto_opened";

    private static final int REQUEST_CODE_ADD_FROM_IMAGE = 0x1020;

    private boolean openedPicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openedPicker = savedInstanceState != null && savedInstanceState.getBoolean(STATE_OPENED_PICKER, false);

        getViewModel(AddAccountFromImageViewModel.class)
                .getResults()
                .observe(this, event -> {
                    event.handle();

                    switch (event.getId()) {
                        case AddAccountFromImageViewModel.RESULT_OK:
                            AddAccountFromImageFragmentDirections.CreateNewAccountFromImageAction action = AddAccountFromImageFragmentDirections.createNewAccountFromImageAction().setUrl(event.getContent());
                            navigateForResultTransfer().navigate(action);
                            break;
                        case AddAccountFromImageViewModel.RESULT_CANCEL:
                            setResultAndLeave(RESULT_CANCELED);
                            break;
                        case AddAccountFromImageViewModel.RESULT_ERROR:
                            // DO NOTHING ?
                            break;
                        case AddAccountFromImageViewModel.RESULT_BROWSE:
                            scanFromImage();
                            break;
                    }
                });
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, AccountFromImageFragmentBinding binding) {
        binding.setViewModel(getViewModel(AddAccountFromImageViewModel.class));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!openedPicker) {
            scanFromImage();
            openedPicker = true;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_OPENED_PICKER, openedPicker);
    }

    private void scanFromImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        Bundle extras = new Bundle();
        extras.putBoolean(Intent.EXTRA_LOCAL_ONLY, true);

        startActivityForResult(intent, REQUEST_CODE_ADD_FROM_IMAGE, extras);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_ADD_FROM_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                getViewModel(AddAccountFromImageViewModel.class).processResults(data);
            } else {
                // canceled
                getViewModel(AddAccountFromImageViewModel.class).onCancelClick(null);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.account_from_image_fragment;
    }
}
