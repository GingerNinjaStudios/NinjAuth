package me.gingerninja.authenticator.data.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.databinding.AccountListItemBinding;
import me.gingerninja.authenticator.util.CodeGenerator;

public class AccountListAdapter extends RecyclerView.Adapter<BindingViewHolder> {
    private List<Account> accountList;
    private CodeGenerator codeGenerator;

    public AccountListAdapter(@NonNull CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    public AccountListAdapter setAccountList(List<Account> accountList) {
        this.accountList = accountList;
        notifyDataSetChanged();
        return this;
    }

    @NonNull
    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingViewHolder<>(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.account_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        AccountListItemBinding listItemBinding = (AccountListItemBinding) holder.getBinding();

        Account account = accountList.get(position);

        listItemBinding.textAccount.setText(account.getAccountName());
        listItemBinding.textCode.setText(codeGenerator.getFormattedCode(account));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BindingViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // TODO add listener
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BindingViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // TODO remove listener
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
