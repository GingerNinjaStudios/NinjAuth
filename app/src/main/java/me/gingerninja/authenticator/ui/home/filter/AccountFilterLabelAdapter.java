package me.gingerninja.authenticator.ui.home.filter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.HashSet;
import java.util.List;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.util.BindingHelpers;

public class AccountFilterLabelAdapter extends RecyclerView.Adapter<AccountFilterLabelAdapter.ChipViewHolder> {
    private LabelFilterListener filterListener;

    private List<Label> labels;
    private HashSet<Label> filterLabels;

    private final CompoundButton.OnCheckedChangeListener checkedChangeListener = (compoundButton, checked) -> {
        Label label = (Label) compoundButton.getTag();

        if (filterListener != null) {
            if (checked) {
                filterListener.onLabelAdded(label);
            } else {
                filterListener.onLabelRemoved(label);
            }
        }
    };

    public AccountFilterLabelAdapter() {
        setHasStableIds(true);
    }

    void setLabels(List<Label> labels) {
        this.labels = labels;
        notifyDataSetChanged();
    }

    void setFilterLabels(HashSet<Label> filterLabels) {
        this.filterLabels = filterLabels;
        notifyDataSetChanged();
    }

    void setFilterListener(LabelFilterListener filterListener) {
        this.filterListener = filterListener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_filter_label_list_item, parent, false);
        return new ChipViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        Label label = labels.get(position % labels.size());
        holder.setFromLabel(label);

        holder.chip.setChecked(filterLabels.contains(label));
        holder.chip.setOnCheckedChangeListener(checkedChangeListener);
    }

    @Override
    public int getItemCount() {
        return labels == null ? 0 : labels.size() * 1;
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        private Chip chip;

        private ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            this.chip = itemView.findViewById(R.id.chip);
        }

        private void setFromLabel(Label label) {
            chip.setTag(label);
            chip.setText(label.getName());
            chip.setChipBackgroundColor(ColorStateList.valueOf(label.getColor()));
            BindingHelpers.setChipTextColor(chip, label.getColor());
        }
    }

    public interface LabelFilterListener {
        void onLabelAdded(Label label);

        void onLabelRemoved(Label label);
    }
}
