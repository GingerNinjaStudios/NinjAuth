package me.gingerninja.authenticator.data.adapter;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.concurrent.TimeUnit;

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
    private boolean dragEnabled = false;

    private int moveFrom = -1, moveTo = -1;

    public AccountListIteratorAdapter(@NonNull AccountWrapper.Factory accountWrapperFactory, @NonNull CodeGenerator codeGenerator, @NonNull AccountRepository accountRepository) {
        setHasStableIds(true);
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
        String type = AccountWrapper.getType(iterator.get(getAdjustedPosition(position)));
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
                onBindHotpViewHolder((HotpViewHolder) holder, getAdjustedPosition(position));
                break;
            case TYPE_ACCOUNT_TOTP:
                onBindTotpViewHolder((TotpViewHolder) holder, getAdjustedPosition(position));
                break;
        }
    }

    private void onBindHotpViewHolder(@NonNull HotpViewHolder holder, int position) {
        AccountListItemHotpBinding listItemBinding = holder.getBinding();
        Account account = accountWrapperFactory.create(iterator.get(position));

        AccountListItemHotpViewModel oldViewModel = listItemBinding.getViewModel();
        if (oldViewModel != null) {
            Account oldAccount = oldViewModel.getAccount();

            if (oldAccount != null && account != null && account.getId() == oldAccount.getId()) {
                oldViewModel.setMode(dragEnabled ? AccountListItemViewModel.MODE_DRAG : AccountListItemViewModel.MODE_IDLE);
                return;
            }
        }

        setupLabels(account, listItemBinding.labels);

        AccountListItemHotpViewModel viewModel = new AccountListItemHotpViewModel(account, codeGenerator, accountRepository);
        viewModel.setMenuItemClickListener(this);
        viewModel.setMode(dragEnabled ? AccountListItemViewModel.MODE_DRAG : AccountListItemViewModel.MODE_IDLE);

        listItemBinding.setViewModel(viewModel);
    }

    private void onBindTotpViewHolder(@NonNull TotpViewHolder holder, int position) {
        AccountListItemTotpBinding listItemBinding = holder.getBinding();
        Account account = accountWrapperFactory.create(iterator.get(position));

        AccountListItemTotpViewModel oldViewModel = listItemBinding.getViewModel();
        if (oldViewModel != null) {
            Account oldAccount = oldViewModel.getAccount();

            if (account.equals(oldAccount)) {
                oldViewModel.setMode(dragEnabled ? AccountListItemViewModel.MODE_DRAG : AccountListItemViewModel.MODE_IDLE);
                return;
            }

            oldViewModel.stopClock();
        }

        setupLabels(account, listItemBinding.labels);

        AccountListItemTotpViewModel viewModel = new AccountListItemTotpViewModel(account, codeGenerator);
        viewModel.setMenuItemClickListener(this);
        viewModel.setMode(dragEnabled ? AccountListItemViewModel.MODE_DRAG : AccountListItemViewModel.MODE_IDLE);

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

    /**
     * Converts the static iterator position to a dynamic position that is adjusted according to the
     * move factors ({@link #moveFrom} and {@link #moveTo}) set by {@link #onItemMove(int, int)}
     * when the user drags a list item to a new position.
     *
     * @param position the position to be converted
     * @return Returns the dynamic position of the item.
     * @see #onItemMove(int, int)
     */
    private int getAdjustedPosition(int position) {
        if (moveFrom < 0 || moveFrom == moveTo) {
            return position;
        } else {
            int min = Math.min(moveFrom, moveTo);
            int max = Math.max(moveFrom, moveTo);

            if (position == moveTo) {
                return moveFrom;
            } else if (position >= min && position <= max) {
                if (moveFrom < moveTo) {
                    return position + 1;
                } else {
                    return position - 1;
                }
            } else {
                return position;
            }
        }
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        if (moveFrom < 0) {
            moveFrom = fromPosition;
        }

        moveTo = toPosition;

        /*if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                //Collections.swap(accountList, i, i + 1);
                int tmp = positions[i];
                positions[i] = positions[i + 1];
                positions[i + 1] = tmp;
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                //Collections.swap(accountList, i, i - 1);
                int tmp = positions[i];
                positions[i] = positions[i - 1];
                positions[i - 1] = tmp;
            }
        }*/

        notifyItemMoved(fromPosition, toPosition); // FIXME the backing iterator does not represent the change so it will not work properly
        return true;
    }

    public void onItemDrag(RecyclerView.ViewHolder viewHolder, boolean isDragging) {
        if (viewHolder == null) {
            return;
        }

        //int viewType = viewHolder.getItemViewType();
        //BindingViewHolder holder = (BindingViewHolder) viewHolder;
        //ViewDataBinding binding = holder.getBinding();

        MaterialCardView cardView = viewHolder.itemView.findViewById(R.id.card);
        if (cardView != null) {
            cardView.clearAnimation();

            float targetElevation = cardView.getResources().getDimension(isDragging ? R.dimen.account_list_card_elevation_dragging : R.dimen.account_list_card_elevation_normal);
            ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "cardElevation", cardView.getCardElevation(), targetElevation);
            animator.start();

            /*int targetPadding = cardView.getResources().getDimensionPixelSize(isDragging ? R.dimen.account_list_card_padding_dragging : R.dimen.account_list_card_padding_normal);
            int originalPadding = cardView.getResources().getDimensionPixelSize(R.dimen.account_list_card_padding_normal);

            ValueAnimator paddingAnimator = ValueAnimator.ofInt(viewHolder.itemView.getPaddingStart(), targetPadding);
            paddingAnimator.addUpdateListener(valueAnimator -> {
                int p = (int) valueAnimator.getAnimatedValue();
                viewHolder.itemView.setPaddingRelative(p, originalPadding, p, originalPadding);
            });

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animator, paddingAnimator);
            set.start();*/
        }

        /*switch (viewType) {
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
        }*/
    }

    public void setDragEnabled(boolean enabled) {
        if (dragEnabled != enabled) {
            dragEnabled = enabled;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onAccountMenuItemClicked(MenuItem item, Account account) {
        if (menuItemClickListener != null) {
            menuItemClickListener.onAccountMenuItemClicked(item, account);
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(getAdjustedPosition(position));
    }

    @Override
    public Tuple getItem(int position) {
        return super.getItem(getAdjustedPosition(position));
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
