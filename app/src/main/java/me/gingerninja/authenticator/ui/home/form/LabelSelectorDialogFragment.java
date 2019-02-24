package me.gingerninja.authenticator.ui.home.form;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.functions.BiConsumer;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.AccountAvailableLabelListAdapter;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.ui.account.BaseAccountViewModel;
import me.gingerninja.authenticator.ui.label.form.LabelEditorFragment;
import me.gingerninja.authenticator.util.resulthandler.FragmentResultListener;
import timber.log.Timber;

public class LabelSelectorDialogFragment extends DialogFragment implements LabelListClickListener, FragmentResultListener {
    public static final int REQUEST_CODE_ADD_NEW_LABEL = 0x9000;

    private static final String TAG = "label-selector";
    private static final String ARG_IDS = "label.used_ids";

    @Inject
    AccountRepository accountRepo;

    private LabelListClickListener clickListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getTargetFragment() instanceof LabelListClickListener) {
            clickListener = (LabelListClickListener) getTargetFragment();
        } else if (getParentFragment() instanceof LabelListClickListener) {
            clickListener = (LabelListClickListener) getParentFragment();
        } else if (context instanceof LabelListClickListener) {
            clickListener = (LabelListClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        clickListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    public static LabelSelectorDialogFragment show(FragmentManager fragmentManager, List<BaseAccountViewModel.LabelData> selectedLabels) {
        long[] usedIds = Observable
                .fromIterable(selectedLabels)
                .collectInto(new long[selectedLabels.size()], new BiConsumer<long[], BaseAccountViewModel.LabelData>() {
                    int i;

                    @Override
                    public void accept(long[] longs, BaseAccountViewModel.LabelData labelData) {
                        longs[i++] = labelData.getLabel().getId();
                    }
                })
                .blockingGet();

        Bundle args = new Bundle();
        args.putLongArray(ARG_IDS, usedIds);

        LabelSelectorDialogFragment fragment = new LabelSelectorDialogFragment();
        fragment.setArguments(args);
        fragment.show(fragmentManager, TAG);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Timber.v("Creating labels dialog");
        long[] usedIds = getArguments().getLongArray(ARG_IDS);
        List<Label> labels = accountRepo.getAllLabel(usedIds).toList().blockingGet();

        Context ctx = getContext();
        View root = LayoutInflater.from(ctx).inflate(R.layout.account_label_list, null);
        RecyclerView recyclerView = root.findViewById(R.id.list);
        recyclerView.setAdapter(new AccountAvailableLabelListAdapter(labels).setLabelListClickListener(this));
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        return new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.account_list_dialog_title)
                .setView(root)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                })
                .setNeutralButton(R.string.account_list_dialog_create_new, (dialog, which) -> {
                    navigateForResult(REQUEST_CODE_ADD_NEW_LABEL).navigate(R.id.labelEditorFragment);
                })
                .create();
    }

    @Override
    public void onLabelSelected(Label label) {
        dismiss();

        if (clickListener != null) {
            clickListener.onLabelSelected(label);
        }
    }

    @Override
    public NavController getNavController() {
        return Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Object data) {
        if (requestCode == REQUEST_CODE_ADD_NEW_LABEL && resultCode == RESULT_OK && data != null) {
            LabelEditorFragment.LabelResult result = (LabelEditorFragment.LabelResult) data;
            onLabelSelected(result.getLabel());
        }
    }
}
