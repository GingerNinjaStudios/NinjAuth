package me.gingerninja.authenticator.data.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.requery.query.Tuple;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.db.wrapper.AccountWrapper;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.databinding.AccountListItemHotpBinding;
import me.gingerninja.authenticator.databinding.AccountListItemTotpBinding;
import me.gingerninja.authenticator.ui.home.list.AccountListItemHotpViewModel;
import me.gingerninja.authenticator.ui.home.list.AccountListItemTotpViewModel;
import me.gingerninja.authenticator.ui.home.list.AccountListItemViewModel;
import me.gingerninja.authenticator.util.BindingHelpers;
import me.gingerninja.authenticator.util.CodeGenerator;

public class AccountListIteratorAdapter extends BaseIteratorAdapter<BindingViewHolder, Tuple> implements AccountListItemViewModel.AccountMenuItemClickListener {
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_ACCOUNT_HOTP = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_ACCOUNT_TOTP = 1;

    private final AccountWrapper.Factory accountWrapperFactory;
    private final CodeGenerator codeGenerator;
    private final AccountRepository accountRepository;

    private AccountListItemViewModel.AccountMenuItemClickListener menuItemClickListener;

    private Disposable disposable;
    private BehaviorSubject<Long> clock = BehaviorSubject.create();

    private int moveFrom = -1, moveTo = -1;

    public AccountListIteratorAdapter(@NonNull AccountWrapper.Factory accountWrapperFactory, @NonNull CodeGenerator codeGenerator, @NonNull AccountRepository accountRepository) {
        this.accountWrapperFactory = accountWrapperFactory;
        this.codeGenerator = codeGenerator;
        this.accountRepository = accountRepository;
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
        String type = AccountWrapper.getType(iterator.get(position));
        switch (type) {
            case Account.TYPE_HOTP:
                return TYPE_ACCOUNT_HOTP;
            case Account.TYPE_TOTP:
                return TYPE_ACCOUNT_TOTP;
            default:
                throw new IllegalArgumentException("Account type not implemented: " + type);

        }
    }

    @NonNull
    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ACCOUNT_HOTP:
                return new HotpViewHolder(parent);
            case TYPE_ACCOUNT_TOTP:
                return new TotpViewHolder(parent);
            default:
                throw new IllegalArgumentException("View type not implemented: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        switch (holder.getViewType()) {
            case TYPE_ACCOUNT_HOTP:
                onBindHotpViewHolder((HotpViewHolder) holder, position);
                break;
            case TYPE_ACCOUNT_TOTP:
                onBindTotpViewHolder((TotpViewHolder) holder, position);
                break;
        }
    }

    private void onBindHotpViewHolder(@NonNull HotpViewHolder holder, int position) {
        AccountListItemHotpBinding listItemBinding = holder.getBinding();

        Account account = accountWrapperFactory.create(iterator.get(position));

        setupLabels(account, listItemBinding.labels);

        AccountListItemHotpViewModel viewModel = new AccountListItemHotpViewModel(account, codeGenerator, accountRepository);
        viewModel.setMenuItemClickListener(this);

        listItemBinding.setViewModel(viewModel);
    }

    private void onBindTotpViewHolder(@NonNull TotpViewHolder holder, int position) {
        AccountListItemTotpBinding listItemBinding = holder.getBinding();

        AccountListItemTotpViewModel oldViewModel = listItemBinding.getViewModel();
        if (oldViewModel != null) {
            oldViewModel.stopClock();
        }

        Account account = accountWrapperFactory.create(iterator.get(position));

        setupLabels(account, listItemBinding.labels);

        AccountListItemTotpViewModel viewModel = new AccountListItemTotpViewModel(account, codeGenerator);
        viewModel.setMenuItemClickListener(this);

        listItemBinding.setViewModel(viewModel);
    }

    private void setupLabels(@NonNull Account account, @NonNull ChipGroup chipGroup) {
        chipGroup.removeAllViews();

        for (Label label : account.getLabels()) {
            int iconRes = label.getIconResourceId();
            Chip chip = new Chip(chipGroup.getContext());
            chip.setChipIconSizeResource(R.dimen.chip_icon_size);
            chip.setIconStartPaddingResource(R.dimen.chip_icon_start_padding);
            if (iconRes != 0) {
                chip.setChipIcon(AppCompatResources.getDrawable(chip.getContext(), iconRes));
            }
            chip.setText(label.getName());
            chip.setChipBackgroundColor(ColorStateList.valueOf(label.getColor()));
            BindingHelpers.setChipTextColor(chip, label.getColor());
            chipGroup.addView(chip, new ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }


    @Override
    public void onViewAttachedToWindow(@NonNull BindingViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        int viewType = holder.getItemViewType();
        ViewDataBinding binding = holder.getBinding();

        switch (viewType) {
            case TYPE_ACCOUNT_TOTP:
                AccountListItemTotpViewModel viewModel = ((AccountListItemTotpBinding) binding).getViewModel();
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
                AccountListItemTotpViewModel viewModel = ((AccountListItemTotpBinding) binding).getViewModel();
                if (viewModel != null) {
                    viewModel.stopClock();
                }
                break;
        }
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
                AccountListItemTotpViewModel viewModel = ((AccountListItemTotpBinding) binding).getViewModel();
                if (viewModel != null) {
                    viewModel.setMode(isDragging ? AccountListItemViewModel.MODE_DRAG : AccountListItemViewModel.MODE_IDLE);
                }
                break;
            case AccountListIteratorAdapter.TYPE_ACCOUNT_HOTP:
                AccountListItemHotpViewModel hotpViewModel = ((AccountListItemHotpBinding) binding).getViewModel();
                if (hotpViewModel != null) {
                    hotpViewModel.setMode(isDragging ? AccountListItemViewModel.MODE_DRAG : AccountListItemViewModel.MODE_IDLE);
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

    @Override
    protected long getItemId(Tuple item) {
        return item.get(Account.ID);
    }

    private static class HotpViewHolder extends BindingViewHolder<AccountListItemHotpBinding> {
        private HotpViewHolder(@NonNull ViewGroup parent) {
            super(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.account_list_item_hotp, parent, false), TYPE_ACCOUNT_HOTP);
        }
    }

    private static class TotpViewHolder extends BindingViewHolder<AccountListItemTotpBinding> {
        private TotpViewHolder(@NonNull ViewGroup parent) {
            super(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.account_list_item_totp, parent, false), TYPE_ACCOUNT_TOTP);
        }
    }
}
