package me.gingerninja.authenticator.ui.backup;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.databinding.DataBindingUtil;

import io.requery.query.Tuple;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.TempLabel;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.databinding.RestoreLabelItemBinding;

public class RestoreLabelAdapter extends BaseRestoreCheckableAdapter<RestoreLabelItemBinding> {
    @NonNull
    private final WorkUpdateHandler workUpdateHandler;

    public RestoreLabelAdapter(@NonNull TemporaryRepository temporaryRepository, @NonNull WorkUpdateHandler workUpdateHandler) {
        super(temporaryRepository);
        this.workUpdateHandler = workUpdateHandler;
    }

    @Override
    protected RestoreLabelItemBinding createDataBinding(@NonNull ViewGroup parent, int viewType) {
        return DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.restore_label_item, parent, false);
    }

    @Override
    protected void onBindViewHolder(@NonNull BaseRestoreViewHolder<RestoreLabelItemBinding> holder, Tuple item) {
        holder.checkbox.setChecked(item.get(TempLabel.RESTORE));

        int restoreMode = item.get(TempLabel.RESTORE_MODE);
        switch (restoreMode) {
            case TempLabel.RestoreMode.INSERT:
                holder.spinner.setValue(RestoreMode.INSERT);
                break;
            case TempLabel.RestoreMode.UPDATE:
                holder.spinner.setValue(RestoreMode.UPDATE);
                break;
        }

        int labelColor = item.get(TempLabel.COLOR);
        boolean isDark = ColorUtils.calculateLuminance(labelColor) < 0.5;
        Resources resources = holder.itemView.getResources();
        int fgColor = isDark ? resources.getColor(R.color.colorLabelTextLight) : resources.getColor(R.color.colorLabelTextDark);


        RestoreLabelItemBinding binding = holder.getBinding();
        binding.labelName.setText(item.get(TempLabel.NAME));
        binding.labelCircle.setImageTintList(ColorStateList.valueOf(labelColor));
        binding.labelIcon.setImageDrawable(TempLabel.getIconDrawable(holder.itemView.getContext(), item.get(TempLabel.ICON)));
        binding.labelIcon.setImageTintList(ColorStateList.valueOf(fgColor));
    }

    @Override
    protected void onItemRestoreStatusChanged(Tuple item, boolean shouldRestore) {
        workUpdateHandler.handleRequest(temporaryRepository.setLabelRestoreEnabled(getItemId(item), shouldRestore));
    }

    @Override
    protected void onItemRestoreModeChanged(Tuple item, @RestoreMode String spinnerMode) {
        final int mode;

        switch (spinnerMode) {
            case RestoreMode.INSERT:
                mode = TempLabel.RestoreMode.INSERT;
                break;
            case RestoreMode.UPDATE:
                mode = TempLabel.RestoreMode.UPDATE;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported restore mode: " + spinnerMode);
        }

        workUpdateHandler.handleRequest(temporaryRepository.setLabelRestoreMode(getItemId(item), mode));
    }

    @Override
    protected long getItemId(Tuple item) {
        return item.get(TempLabel.ID);
    }
}
