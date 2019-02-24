package me.gingerninja.authenticator.data.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.ui.account.BaseAccountViewModel;
import me.gingerninja.authenticator.ui.home.form.LabelClickListener;
import me.gingerninja.authenticator.util.BindingHelpers;
import timber.log.Timber;

public class AccountLabelListAdapter extends RecyclerView.Adapter<AccountLabelListAdapter.ChipViewHolder> implements LabelClickListener {
    private static final int TYPE_LABEL = R.layout.account_form_label_list_entry;
    private static final int TYPE_ADD_BUTTON = R.layout.account_form_label_list_add_item;

    @Nullable
    private List<BaseAccountViewModel.LabelData> labels;

    private View.OnClickListener labelRemoveClickListener = v -> onLabelRemoved((Label) v.getTag(), -1);
    private View.OnClickListener labelAddClickListener = this::onLabelAddClicked;

    @Nullable
    private LabelClickListener labelClickListener;

    private Disposable labelsDisposable;

    public AccountLabelListAdapter(@NonNull Single<List<BaseAccountViewModel.LabelData>> labels) {
        setHasStableIds(true);

        labelsDisposable = labels.subscribe((labelData, throwable) -> {
            this.labels = labelData;
            notifyDataSetChanged();
        });
    }

    public AccountLabelListAdapter setLabelClickListener(@Nullable LabelClickListener labelClickListener) {
        this.labelClickListener = labelClickListener;
        return this;
    }

    public void addLabel(@NonNull Label label) {
        this.labels.add(new BaseAccountViewModel.LabelData(label, this.labels.size()));
        notifyDataSetChanged();
    }

    public void addLabel(@NonNull Label label, int position) {
        this.labels.add(Math.min(position, labels.size()), new BaseAccountViewModel.LabelData(label, this.labels.size()));
        notifyDataSetChanged();
    }

    @Nullable
    public List<BaseAccountViewModel.LabelData> getLabels() {
        return labels;
    }

    @NonNull
    @Override
    public AccountLabelListAdapter.ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChipViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountLabelListAdapter.ChipViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_LABEL) {
            holder.chip.setOnCloseIconClickListener(labelRemoveClickListener);
            //holder.chip.setOnClickListener(null);
            holder.setFromLabel(labels.get(position).getLabel());
        } else {
            //holder.chip.setOnCloseIconClickListener(null);
            holder.chip.setOnClickListener(labelAddClickListener);
        }
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_LABEL) {
            return labels.get(position).getLabel().getId();
        }

        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_ADD_BUTTON;
        }

        return TYPE_LABEL;
    }

    @Override
    public int getItemCount() {
        return labels == null ? 1 : labels.size() + 1;
    }

    @Override
    public void onLabelAddClicked(View view) {
        if (labelClickListener != null) {
            labelClickListener.onLabelAddClicked(view);
        }
    }

    @Override
    public void onLabelRemoved(Label label, int fakePos) {
        if (labelClickListener != null) {
            int i = 0;
            for (Iterator<BaseAccountViewModel.LabelData> it = labels.iterator(); it.hasNext(); i++) {
                BaseAccountViewModel.LabelData labelData = it.next();
                if (labelData.getLabel().getId() == label.getId()) {
                    it.remove();
                    labelClickListener.onLabelRemoved(label, i);
                    break;
                }
            }
            // labels.remove(label);
            notifyDataSetChanged();
        }
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        Timber.v("onItemMove() - from: %d, to: %d", fromPosition, toPosition);
        toPosition = Math.min(labels.size() - 1, toPosition);

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(labels, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(labels, i, i - 1);
            }
        }

        for (int i = 0; i < labels.size(); i++) {
            BaseAccountViewModel.LabelData label = labels.get(i);
            if (label.getPosition() != i) {
                label.setPosition(i);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public void onItemDrag(RecyclerView.ViewHolder viewHolder, boolean isDragging) {
        if (viewHolder == null) {
            return;
        }

        ChipViewHolder holder = (ChipViewHolder) viewHolder;
        holder.chip.setAlpha(isDragging ? .75f : 1f);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        if (labelsDisposable != null) {
            labelsDisposable.dispose();
        }
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        private Chip chip;

        private ChipViewHolder(@NonNull View itemView) {
            super(itemView);

            if (itemView instanceof Chip) {
                this.chip = (Chip) itemView;
            } else {
                throw new IllegalArgumentException("Chip was expected, found: " + itemView.getClass().getSimpleName());
            }
        }

        private void setFromLabel(Label label) {
            chip.setTag(label);
            chip.setText(label.getName());
            chip.setChipBackgroundColor(ColorStateList.valueOf(label.getColor()));
            BindingHelpers.setChipTextColor(chip, label.getColor());
            // TODO chip.setTextColor();
        }
    }
}
