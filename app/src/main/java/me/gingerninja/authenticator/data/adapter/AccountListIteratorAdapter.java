package me.gingerninja.authenticator.data.adapter;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.requery.query.Tuple;
import io.requery.sql.ResultSetIterator;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.db.wrapper.AccountWrapper;
import me.gingerninja.authenticator.databinding.AccountListItemBinding;
import me.gingerninja.authenticator.ui.home.list.AccountListItemViewModel;
import me.gingerninja.authenticator.util.BindingHelpers;
import me.gingerninja.authenticator.util.CodeGenerator;

public class AccountListIteratorAdapter extends RecyclerView.Adapter<BindingViewHolder> implements AccountListItemViewModel.AccountMenuItemClickListener {
    public static final int TYPE_ACCOUNT_TOTP = 1;

    private final AccountWrapper.Factory accountWrapperFactory;
    private final CodeGenerator codeGenerator;

    private ResultSetIterator<Tuple> iterator;

    private AccountListItemViewModel.AccountMenuItemClickListener menuItemClickListener;

    private Disposable disposable;
    private BehaviorSubject<Long> clock = BehaviorSubject.create();

    private int moveFrom = -1, moveTo = -1;

    public AccountListIteratorAdapter(@NonNull AccountWrapper.Factory accountWrapperFactory, @NonNull CodeGenerator codeGenerator) {
        this.accountWrapperFactory = accountWrapperFactory;
        this.codeGenerator = codeGenerator;
    }

    public void setResults(ResultSetIterator<Tuple> iterator) {
        if (this.iterator == iterator) {
            return;
        }

        if (this.iterator != null) {
            this.iterator.close();
        }
        this.iterator = iterator;

        notifyDataSetChanged();
    }

    public void close() {
        if (iterator != null) {
            iterator.close();
            iterator = null;
        }
    }

    /**
     * Returns a two-long array containing the from and to positions, respectively. It also resets
     * the values to their initial state.
     *
     * @return a two-long array containing the from and to positions, respectively
     */
    @Size(2)
    public int[] getMovementAndReset() {
        int[] ret = new int[]{moveFrom, moveTo};

        moveFrom = -1;
        moveTo = -1;

        return ret;
    }

    public void setMenuItemClickListener(AccountListItemViewModel.AccountMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    public void startClock() {
        stopClock();

        disposable = Observable.interval(100, TimeUnit.MILLISECONDS).subscribe(clock::onNext);
    }

    public void stopClock() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ACCOUNT_TOTP;
    }

    @NonNull
    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingViewHolder<>(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.account_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        AccountListItemBinding listItemBinding = (AccountListItemBinding) holder.getBinding();

        AccountListItemViewModel oldViewModel = listItemBinding.getViewModel();
        if (oldViewModel != null) {
            oldViewModel.stopClock();
        }

        Account account = accountWrapperFactory.create(iterator.get(position));

        ChipGroup chipGroup = holder.itemView.findViewById(R.id.labels);
        chipGroup.removeAllViews();

        for (Label label : account.getLabels()) {
            Chip chip = new Chip(chipGroup.getContext());
            chip.setText(label.getName());
            chip.setChipBackgroundColor(ColorStateList.valueOf(label.getColor()));
            BindingHelpers.setChipTextColor(chip, label.getColor());
            chipGroup.addView(chip, new ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        listItemBinding.setViewModel(new AccountListItemViewModel(account, codeGenerator).setMenuItemClickListener(this));
    }


    @Override
    public void onViewAttachedToWindow(@NonNull BindingViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        int viewType = holder.getItemViewType();
        ViewDataBinding binding = holder.getBinding();

        switch (viewType) {
            case TYPE_ACCOUNT_TOTP:
                AccountListItemViewModel viewModel = ((AccountListItemBinding) binding).getViewModel();
                if (viewModel != null) {
                    viewModel.startClock(clock);
                }
                break;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BindingViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        int viewType = holder.getItemViewType();
        ViewDataBinding binding = holder.getBinding();

        switch (viewType) {
            case TYPE_ACCOUNT_TOTP:
                AccountListItemViewModel viewModel = ((AccountListItemBinding) binding).getViewModel();
                if (viewModel != null) {
                    viewModel.stopClock();
                }
                break;
        }
    }

    @Override
    public long getItemId(int position) {
        return iterator == null ? RecyclerView.NO_ID : iterator.get(position).get(Account.ID);
    }

    @Override
    public int getItemCount() {
        if (iterator == null) {
            return 0;
        }

        try {
            Cursor cursor = iterator.unwrap(Cursor.class);
            return cursor.getCount();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        close();
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        if (moveFrom < 0) {
            moveFrom = fromPosition;
        }

        moveTo = toPosition;

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public void onItemDrag(RecyclerView.ViewHolder viewHolder, boolean isDragging) {
        if (viewHolder == null) {
            return;
        }

        int viewType = viewHolder.getItemViewType();
        BindingViewHolder holder = (BindingViewHolder) viewHolder;
        ViewDataBinding binding = holder.getBinding();

        switch (viewType) {
            case AccountListIteratorAdapter.TYPE_ACCOUNT_TOTP:
                AccountListItemViewModel viewModel = ((AccountListItemBinding) binding).getViewModel();
                if (viewModel != null) {
                    viewModel.setMode(isDragging ? AccountListItemViewModel.MODE_DRAG : AccountListItemViewModel.MODE_IDLE);
                }
                break;
        }
    }

    @Override
    public void onAccountMenuItemClicked(MenuItem item, Account account) {
        if (menuItemClickListener != null) {
            menuItemClickListener.onAccountMenuItemClicked(item, account);
        }
    }
}
