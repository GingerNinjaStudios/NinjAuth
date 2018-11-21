package me.gingerninja.authenticator.data.adapter;

import android.view.LayoutInflater;
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

public class AccountListAdapter extends RecyclerView.Adapter<BindingViewHolder> {
    private List<Account> accountList;
    private CodeGenerator codeGenerator;

    private Disposable disposable;
    private BehaviorSubject<Long> clock = BehaviorSubject.create();

    public AccountListAdapter(@NonNull CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    public AccountListAdapter setAccountList(List<Account> accountList) {
        this.accountList = accountList;
        notifyDataSetChanged();
        return this;
    }

    public void startClock() {
        stopClock();

        disposable = Observable.interval(100, TimeUnit.MILLISECONDS).subscribe(v -> {
            clock.onNext(v);
        });
    }

    public void stopClock() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
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

        listItemBinding.setViewModel(new AccountListItemViewModel(accountList.get(position), codeGenerator));

        /*Account account = accountList.get(position);

        listItemBinding.textAccount.setText(account.getAccountName());
        listItemBinding.textCode.setText(codeGenerator.getFormattedCode(account));*/
    }


    @Override
    public void onViewAttachedToWindow(@NonNull BindingViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // TODO add listener

        ViewDataBinding binding = holder.getBinding();

        if (binding instanceof AccountListItemBinding) {
            AccountListItemViewModel viewModel = ((AccountListItemBinding) binding).getViewModel();
            if (viewModel != null) {
                viewModel.startClock(clock);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BindingViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // TODO remove listener

        ViewDataBinding binding = holder.getBinding();

        if (binding instanceof AccountListItemBinding) {
            AccountListItemViewModel viewModel = ((AccountListItemBinding) binding).getViewModel();
            if (viewModel != null) {
                viewModel.stopClock();
            }
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
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
}
