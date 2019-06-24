package me.gingerninja.authenticator.ui.backup;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.util.SingleEvent;
import me.gingerninja.authenticator.util.backup.Backup;

public class BackupDialogViewModel extends ViewModel {
    public static final int STATE_ERROR = -1;
    public static final int STATE_IN_PROGRESS = 0;
    public static final int STATE_COMPLETE = 1;

    public static final String EVENT_DISMISS_SUCCESS = "dismiss.success";
    public static final String EVENT_DISMISS_ERROR = "dismiss.error";

    public ObservableInt state = new ObservableInt(STATE_IN_PROGRESS);

    public ObservableInt currentProgress = new ObservableInt(0);
    public ObservableInt maxProgress = new ObservableInt(0);
    public ObservableBoolean indeterminate = new ObservableBoolean(true);
    public ObservableField<String> progressMessage = new ObservableField<>();
    public ObservableField<String> errorMessage = new ObservableField<>();

    @NonNull
    private Context context;
    private Disposable disposable;

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    @Inject
    public BackupDialogViewModel(@NonNull Context context) {
        this.context = context;
    }

    @Override
    protected void onCleared() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void updateProgress(Backup.Progress progress) {
        indeterminate.set(progress.maxProgress == 0);

        if (progress.maxProgress > 0) {
            currentProgress.set(progress.currentProgress);
            maxProgress.set(progress.maxProgress);
        }

        String msg = null;

        switch (progress.phaseId) {
            case Backup.Progress.PHASE_DATA_FILE:
                msg = context.getString(R.string.backup_progress_msg_data);
                break;
            case Backup.Progress.PHASE_ACCOUNT_IMAGES:
                msg = context.getString(R.string.backup_progress_msg_account_images, progress.currentProgress, progress.maxProgress);
                break;
            case Backup.Progress.PHASE_FINALIZING_ZIP:
                msg = context.getString(R.string.backup_progress_msg_finalizing);
                break;
        }

        progressMessage.set(msg);
    }

    private void handleBackupError(Throwable throwable) {
        errorMessage.set(context.getString(R.string.backup_error_generic));
        state.set(STATE_ERROR);
    }

    private void handleBackupComplete() {
        state.set(STATE_COMPLETE);
    }

    LiveData<SingleEvent> getEvents() {
        return events;
    }

    public void onSuccessOkClick(View v) {
        events.setValue(new SingleEvent(EVENT_DISMISS_SUCCESS));
    }

    public void onErrorOkClick(View v) {
        events.setValue(new SingleEvent(EVENT_DISMISS_ERROR));
    }

    void setupWithParentViewModel(BackupViewModel backupViewModel) {
        if (disposable != null) {
            return;
        }

        disposable = backupViewModel.observeProgress().subscribe(this::updateProgress, this::handleBackupError, this::handleBackupComplete);
    }
}
