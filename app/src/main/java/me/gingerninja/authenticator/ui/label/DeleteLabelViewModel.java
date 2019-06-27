package me.gingerninja.authenticator.ui.label;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

import javax.inject.Inject;

import me.gingerninja.authenticator.data.repo.AccountRepository;
import timber.log.Timber;

public class DeleteLabelViewModel extends ViewModel {
    static final String ACTION_CANCEL = "cancel";
    static final String ACTION_DELETE = "delete";

    private long labelId;

    @NonNull
    private String labelTitle;

    private AccountRepository accountRepository;

    private MutableLiveData<String> action = new MutableLiveData<>();

    @Inject
    public DeleteLabelViewModel(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    void setData(@NonNull Bundle args) {
        labelId = args.getLong(DeleteLabelBottomFragment.ARG_LABEL_ID);
        labelTitle = Objects.requireNonNull(args.getString(DeleteLabelBottomFragment.ARG_LABEL_NAME));
    }

    @NonNull
    public String getLabelTitle() {
        return labelTitle;
    }

    public void onCancelClick(View view) {
        action.setValue(ACTION_CANCEL);
    }

    public void onDeleteClick(View view) {
        accountRepository
                .deleteLabel(labelId)
                .subscribe(() -> action.postValue(ACTION_DELETE), throwable -> {
                    Timber.e(throwable, "Cannot delete account");
                    // TODO
                });
    }

    public LiveData<String> getAction() {
        return action;
    }
}
