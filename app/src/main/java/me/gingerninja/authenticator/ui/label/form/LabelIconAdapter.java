package me.gingerninja.authenticator.ui.label.form;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;

public class LabelIconAdapter extends RecyclerView.Adapter<LabelIconAdapter.VH> implements View.OnClickListener {
    private final String[] icons;

    @Nullable
    private String selected;

    @Nullable
    private IconListener listener;

    public LabelIconAdapter(@NonNull String[] icons) {
        this.icons = icons;
    }

    public void setIconListener(IconListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.label_icon_item, parent, false);
        return new VH(root);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String icon = icons[position];
        holder.card.setTag(icon);
        holder.card.setChecked(TextUtils.equals(icon, selected));
        holder.card.setOnClickListener(this);
        holder.icon.setImageDrawable(Label.getIconDrawable(holder.icon.getContext(), icon));
    }

    @Override
    public int getItemCount() {
        return icons.length;
    }

    void setSelectedIcon(@Nullable String selected) {
        this.selected = selected;
    }

    @Override
    public void onClick(View view) {
        String icon = (String) view.getTag();
        if (listener != null) {
            listener.onIconSelected(icon);
        }
    }

    public interface IconListener {
        void onIconSelected(String icon);
    }

    static class VH extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final ImageView icon;

        private VH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
