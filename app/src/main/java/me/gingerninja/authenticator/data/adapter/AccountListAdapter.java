package me.gingerninja.authenticator.data.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.databinding.AccountListItemBinding;
import me.gingerninja.authenticator.ui.home.list.AccountListItemViewModel;
import me.gingerninja.authenticator.util.CodeGenerator;

public class AccountListAdapter extends RecyclerView.Adapter<BindingViewHolder> implements AccountListItemViewModel.AccountMenuItemClickListener {
    public static final int TYPE_ACCOUNT_TOTP = 1;

    private List<Account> accountList;
    private CodeGenerator codeGenerator;
    private AccountListItemViewModel.AccountMenuItemClickListener menuItemClickListener;

    private Disposable disposable;
    private BehaviorSubject<Long> clock = BehaviorSubject.create();

    public AccountListAdapter(@NonNull CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public AccountListAdapter setAccountList(List<Account> accountList) {
        this.accountList = accountList;
        notifyDataSetChanged();
        return this;
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

        listItemBinding.setViewModel(new AccountListItemViewModel(accountList.get(position), codeGenerator).setMenuItemClickListener(this));

        /*Account account = accountList.get(position);

        listItemBinding.textAccount.setText(account.getAccountName());
        listItemBinding.textCode.setText(codeGenerator.getFormattedCode(account));*/
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
        return accountList == null ? RecyclerView.NO_ID : accountList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return accountList != null ? accountList.size() : 0;
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(accountList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(accountList, i, i - 1);
            }
        }

        for (int i = 0; i < accountList.size(); i++) {
            Account account = accountList.get(i);
            if (account.getPosition() != i) {
                account.setPosition(i);
            }
        }

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
            case AccountListAdapter.TYPE_ACCOUNT_TOTP:
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
