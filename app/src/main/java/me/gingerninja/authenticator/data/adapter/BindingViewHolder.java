package me.gingerninja.authenticator.data.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

class BindingViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {
    @NonNull
    private T binding;

    public BindingViewHolder(@NonNull T binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @NonNull
    public T getBinding() {
        return binding;
    }
}
