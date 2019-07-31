package me.gingerninja.authenticator.ui.backup;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.databinding.ViewDataBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.requery.query.Tuple;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.BaseIteratorAdapter;
import me.gingerninja.authenticator.data.adapter.BindingViewHolder;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.widget.MaterialSpinner;

public abstract class BaseRestoreCheckableAdapter<T extends ViewDataBinding> extends BaseIteratorAdapter<BaseRestoreCheckableAdapter.BaseRestoreViewHolder<T>, Tuple> {
    @NonNull
    protected TemporaryRepository temporaryRepository;

    public BaseRestoreCheckableAdapter(@NonNull TemporaryRepository temporaryRepository) {
        setHasStableIds(true);
        this.temporaryRepository = temporaryRepository;
    }

    protected abstract T createDataBinding(@NonNull ViewGroup parent, int viewType);

    protected abstract void onBindViewHolder(@NonNull BaseRestoreViewHolder<T> holder, Tuple item);

    protected abstract void onItemRestoreStatusChanged(Tuple item, boolean shouldRestore);

    protected abstract void onItemRestoreModeChanged(Tuple item, @RestoreMode String mode);

    @NonNull
    @Override
    public BaseRestoreViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BaseRestoreViewHolder<>(createDataBinding(parent, viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseRestoreViewHolder<T> holder, int position) {
        Tuple item = getItem(position);

        if (holder.tuple != null && getItemId(holder.tuple) == getItemId(item)) {
            return;
        }

        holder.bind(item);

        holder.container.setOnClickListener(view -> holder.checkbox.setChecked(!holder.checkbox.isChecked()));

        holder.checkbox.setOnCheckedChangeListener(null);
        holder.spinner.setOnSpinnerChangeListener(null);

        onBindViewHolder(holder, item);

        holder.checkbox.setOnCheckedChangeListener(this::onCheckedChanged);
        holder.spinner.setOnSpinnerChangeListener(this::onSpinnerChanged);
    }

    private void onCheckedChanged(CompoundButton view, boolean isChecked) {
        onItemRestoreStatusChanged((Tuple) view.getTag(), isChecked);
    }

    private boolean onSpinnerChanged(MaterialSpinner view, CharSequence newValue, CharSequence oldValue) {
        onItemRestoreModeChanged((Tuple) view.getTag(), newValue != null ? newValue.toString() : null);
        return true;
    }

    @StringDef({RestoreMode.INSERT, RestoreMode.UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    protected @interface RestoreMode {
        String INSERT = "insert"; // maps @string/restore_mode_insert_value
        String UPDATE = "update"; // maps @string/restore_mode_update_value
    }

    public static class BaseRestoreViewHolder<T extends ViewDataBinding> extends BindingViewHolder<T> {
        protected CompoundButton checkbox;
        protected View container;
        protected MaterialSpinner spinner;

        private Tuple tuple;

        public BaseRestoreViewHolder(@NonNull T binding) {
            super(binding);
            container = itemView.findViewById(R.id.card_container);
            checkbox = itemView.findViewById(R.id.restore_checkbox);
            spinner = itemView.findViewById(R.id.restore_mode_spinner);
        }

        public void bind(Tuple tuple) {
            this.tuple = tuple;
            checkbox.setTag(tuple);
            spinner.setTag(tuple);
        }
    }
}
