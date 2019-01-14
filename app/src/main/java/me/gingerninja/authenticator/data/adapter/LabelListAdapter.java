package me.gingerninja.authenticator.data.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.LabelListItemBinding;
import me.gingerninja.authenticator.ui.label.LabelListItemViewModel;

public class LabelListAdapter extends RecyclerView.Adapter<BindingViewHolder> {
    private List<Label> labelList;

    @NonNull
    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingViewHolder<>(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.label_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        LabelListItemBinding binding = (LabelListItemBinding) holder.getBinding();
        binding.setViewModel(new LabelListItemViewModel(labelList.get(position)));
    }

    @Override
    public int getItemCount() {
        return labelList == null ? 0 : labelList.size();
    }

    public void setLabelList(List<Label> labelList) {
        this.labelList = labelList;
        notifyDataSetChanged();
    }
}
