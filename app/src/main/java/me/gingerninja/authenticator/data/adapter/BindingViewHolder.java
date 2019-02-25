package me.gingerninja.authenticator.data.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

class BindingViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {
    @NonNull
    private final T binding;

    private final int viewType;

    public BindingViewHolder(@NonNull T binding) {
        this(binding, 0);
    }

    public BindingViewHolder(@NonNull T binding, int viewType) {
        super(binding.getRoot());
        this.binding = binding;
        this.viewType = viewType;
    }

    @NonNull
    public T getBinding() {
        return binding;
    }

    public int getViewType() {
        return viewType;
    }
}
