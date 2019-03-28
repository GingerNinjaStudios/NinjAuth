package me.gingerninja.authenticator.data.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.LabelListItemColorBinding;
import me.gingerninja.authenticator.ui.label.LabelListItemViewModel;

public class LabelListAdapter extends RecyclerView.Adapter<BindingViewHolder> implements LabelListItemViewModel.LabelMenuItemClickListener {
    private List<Label> labelList;

    private LabelListItemViewModel.LabelMenuItemClickListener menuItemClickListener;

    @NonNull
    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingViewHolder<>(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.label_list_item_color, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        LabelListItemColorBinding binding = (LabelListItemColorBinding) holder.getBinding();
        binding.setViewModel(new LabelListItemViewModel(labelList.get(position), holder.itemView).setMenuItemClickListener(this));
    }

    @Override
    public int getItemCount() {
        return labelList == null ? 0 : labelList.size();
    }

    public void setLabelList(List<Label> labelList) {
        this.labelList = labelList;
        notifyDataSetChanged();
    }

    public void setMenuItemClickListener(LabelListItemViewModel.LabelMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    @Override
    public void onLabelMenuItemClicked(MenuItem item, Label label) {
        if (menuItemClickListener != null) {
            menuItemClickListener.onLabelMenuItemClicked(item, label);
        }
    }
}
