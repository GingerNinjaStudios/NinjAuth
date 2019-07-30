package me.gingerninja.authenticator.ui.backup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.FragmentNavigator;

import com.google.android.material.snackbar.Snackbar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.RestoreFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public class RestoreFragment extends BaseFragment<RestoreFragmentBinding> {
    private CompositeDisposable disposable = new CompositeDisposable();

    private OnBackPressedCallback backButtonCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // TODO confirmation dialog
            setResultAndLeave(RESULT_CANCELED);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        assert activity != null;
        activity.getOnBackPressedDispatcher().addCallback(this, backButtonCallback);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, RestoreFragmentBinding binding) {
        subscribeToUi(binding);
    }

    private void subscribeToUi(RestoreFragmentBinding binding) {
        binding.toolbar.setNavigationOnClickListener(v -> getNavController().navigateUp());

        RestoreViewModel viewModel = getViewModel(RestoreViewModel.class);

        binding.setViewModel(viewModel);

        assert getArguments() != null;
        disposable.add(
                viewModel
                        .startRestore(getArguments())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleRestoreEvents, this::handleRestoreError, this::handleRestoreComplete)
        );

        binding.accountsSelector.setOnClickListener(view -> {
            RestoreFragmentDirections.OpenRestoreContentListAction action = RestoreFragmentDirections.openRestoreContentListAction(RestoreContentListFragment.Type.ACCOUNTS);

            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(binding.accountsSelector, "accounts")
                    .build();

            getNavController().navigate(action);
        });

        binding.labelsSelector.setOnClickListener(view -> {
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(binding.labelsSelector, "labels")
                    .build();

            RestoreFragmentDirections.OpenRestoreContentListAction action = RestoreFragmentDirections.openRestoreContentListAction(RestoreContentListFragment.Type.LABELS);
            getNavController().navigate(action);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (disposable != null) {
            disposable.clear();
        }
    }

    private void handleRestoreComplete() {
        /*BackupFile backupFile = getViewModel(RestoreViewModel.class).getBackupFile();
        if (backupFile != null) {
            // TODO
            Timber.v("Restore complete, accounts: %s, labels: %s", backupFile.getAccounts(), backupFile.getLabels());
        } else {
            Timber.v("Restore complete, no data");
        }*/

        Snackbar.make(getView(), "Restore complete", Snackbar.LENGTH_LONG).show();
        setResultAndLeave(RESULT_OK);
    }

    private void handleRestoreEvents(@NonNull SingleEvent event) {
        if (event.handle()) {
            switch (event.getId()) {
                case RestoreViewModel.ACTION_RESTORE_PASSWORD_NEEDED:
                    RestorePasswordDialogFragment.show(getChildFragmentManager(), false);
                    break;
                case RestoreViewModel.ACTION_RESTORE_WRONG_PASSWORD:
                    RestorePasswordDialogFragment.show(getChildFragmentManager(), true);
                    break;
                case RestoreViewModel.ACTION_DATA_LOADED:
                    // TODO
                    Snackbar.make(getView(), "Restore data loaded", Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private void handleRestoreError(Throwable throwable) {
        Timber.e(throwable, "Restore error");

        if (throwable instanceof UserCanceledRestoreException) {
            setResultAndLeave(RESULT_CANCELED);
        }

        //Snackbar.make(getView(), "Restore error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.restore_fragment;
    }
}
