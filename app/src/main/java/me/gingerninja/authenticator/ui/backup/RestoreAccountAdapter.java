package me.gingerninja.authenticator.ui.backup;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import io.requery.query.Tuple;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.TempAccount;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.databinding.RestoreAccountItemBinding;

public class RestoreAccountAdapter extends BaseRestoreCheckableAdapter<RestoreAccountItemBinding> {
    @NonNull
    private final WorkUpdateHandler workUpdateHandler;

    public RestoreAccountAdapter(@NonNull TemporaryRepository temporaryRepository, @NonNull WorkUpdateHandler workUpdateHandler) {
        super(temporaryRepository);
        this.workUpdateHandler = workUpdateHandler;
    }

    @Override
    protected RestoreAccountItemBinding createDataBinding(@NonNull ViewGroup parent, int viewType) {
        return DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.restore_account_item, parent, false);
    }

    @Override
    protected void onBindViewHolder(@NonNull BaseRestoreViewHolder<RestoreAccountItemBinding> holder, Tuple item) {
        holder.checkbox.setChecked(item.get(TempAccount.RESTORE));

        int restoreMode = item.get(TempAccount.RESTORE_MODE);
        switch (restoreMode) {
            case TempAccount.RestoreMode.INSERT:
                holder.spinner.setValue(RestoreMode.INSERT);
                break;
            case TempAccount.RestoreMode.UPDATE:
                holder.spinner.setValue(RestoreMode.UPDATE);
                break;
        }

        RestoreAccountItemBinding binding = holder.getBinding();
        binding.title.setText(item.get(TempAccount.TITLE));
        binding.accountName.setText(item.get(TempAccount.ACCOUNT_NAME));
    }

    @Override
    protected void onItemRestoreStatusChanged(Tuple item, boolean shouldRestore) {
        workUpdateHandler.handleRequest(temporaryRepository.setAccountRestoreEnabled(getItemId(item), shouldRestore));
    }

    @Override
    protected void onItemRestoreModeChanged(Tuple item, @RestoreMode String spinnerMode) {
        final int mode;

        switch (spinnerMode) {
            case RestoreMode.INSERT:
                mode = TempAccount.RestoreMode.INSERT;
                break;
            case RestoreMode.UPDATE:
                mode = TempAccount.RestoreMode.UPDATE;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported restore mode: " + spinnerMode);
        }

        workUpdateHandler.handleRequest(temporaryRepository.setAccountRestoreMode(getItemId(item), mode));
    }

    @Override
    protected long getItemId(Tuple item) {
        return item.get(TempAccount.ID);
    }
}
