package me.gingerninja.authenticator.data.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.ui.home.form.LabelListClickListener;
import me.gingerninja.authenticator.util.BindingHelpers;

public class AccountAvailableLabelListAdapter extends RecyclerView.Adapter<AccountAvailableLabelListAdapter.ChipViewHolder> implements LabelListClickListener {
    @NonNull
    private List<Label> labels;

    private View.OnClickListener labelSelectedClickListener = v -> onLabelSelected((Label) v.getTag());

    @Nullable
    private LabelListClickListener labelListClickListener;

    public AccountAvailableLabelListAdapter(@NonNull List<Label> labels) {
        this.labels = labels;
    }

    public AccountAvailableLabelListAdapter setLabelListClickListener(@Nullable LabelListClickListener labelListClickListener) {
        this.labelListClickListener = labelListClickListener;
        return this;
    }

    @NonNull
    @Override
    public AccountAvailableLabelListAdapter.ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChipViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.account_label_list_entry_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountAvailableLabelListAdapter.ChipViewHolder holder, int position) {
        holder.itemView.setOnClickListener(labelSelectedClickListener);
        holder.setFromLabel(labels.get(position));
    }

    @Override
    public long getItemId(int position) {
        return labels.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    @Override
    public void onLabelSelected(Label label) {
        if (labelListClickListener != null) {
            labelListClickListener.onLabelSelected(label);
        }
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        private Chip chip;

        private ChipViewHolder(@NonNull View itemView) {
            super(itemView);

            chip = itemView.findViewById(R.id.chip);
        }

        private void setFromLabel(Label label) {
            itemView.setTag(label);
            chip.setText(label.getName());
            chip.setChipBackgroundColor(ColorStateList.valueOf(label.getColor()));
            BindingHelpers.setChipTextColor(chip, label.getColor());
        }
    }
}
