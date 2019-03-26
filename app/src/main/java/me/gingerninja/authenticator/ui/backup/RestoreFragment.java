package me.gingerninja.authenticator.ui.backup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.viewpager.widget.ViewPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.pojo.BackupFile;
import me.gingerninja.authenticator.databinding.RestoreFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.SingleEvent;
import timber.log.Timber;

public class RestoreFragment extends BaseFragment<RestoreFragmentBinding> implements OnBackPressedCallback {
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, RestoreFragmentBinding binding) {
        subscribeToUi(binding);
    }

    private void subscribeToUi(RestoreFragmentBinding binding) {
        binding.toolbar.setNavigationOnClickListener(v -> getNavController().navigateUp());

        // overcoming MaterialButton's inability to set the icon to the end
        binding.btnNext.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getActivity(), R.drawable.ic_chevron_next_24dp), null);

        binding.btnPrev.setOnClickListener(this::handleBackButton);
        binding.btnNext.setOnClickListener(this::handleNextButton);

        // TODO binding.viewPager.setAdapter(pagerAdapter);
        binding.progressIndicator.setViewPager(binding.viewPager);
        binding.progressIndicator.setDotsClickable(false);

        assert getArguments() != null;
        disposable.add(
                getViewModel(RestoreViewModel.class)
                        .startRestore(getArguments())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleRestoreEvents, this::handleRestoreError, this::handleRestoreComplete)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void handleNextButton(View v) {
        ViewPager viewPager = getDataBinding().viewPager;

        boolean isFinalPage = true;// TODO viewPager.getCurrentItem() == pagerAdapter.getCount() - 1;

        if (isFinalPage) {
            // TODO
            setResultAndLeave(RESULT_OK);
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        }
    }

    private void handleBackButton(View v) {
        if (getDataBinding().viewPager.getCurrentItem() <= 0) {
            setResultAndLeave(RESULT_CANCELED);
        } else {
            handleOnBackPressed();
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
    }

    private void handleRestoreEvents(@NonNull SingleEvent<BackupFile> backupFileEvent) {
        if (backupFileEvent.isHandled()) {
            return;
        }

        switch (backupFileEvent.getId()) {
            case RestoreViewModel.ACTION_RESTORE_PASSWORD_NEEDED:
                RestorePasswordDialogFragment.show(getChildFragmentManager(), false);
                backupFileEvent.handle();
                break;
            case RestoreViewModel.ACTION_RESTORE_WRONG_PASSWORD:
                RestorePasswordDialogFragment.show(getChildFragmentManager(), true);
                backupFileEvent.handle();
                break;
        }
    }

    private void handleRestoreError(Throwable throwable) {
        Timber.e(throwable, "Restore error");
        // TODO
        if (throwable instanceof UserCanceledRestoreException) {
            setResultAndLeave(RESULT_CANCELED);
        }

        Snackbar.make(getView(), "Restore error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.restore_fragment;
    }

    @Override
    public boolean handleOnBackPressed() {
        boolean handled = false;

        ViewPager viewPager = getDataBinding().viewPager;

        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            handled = true;
        }

        return handled;
    }
}
