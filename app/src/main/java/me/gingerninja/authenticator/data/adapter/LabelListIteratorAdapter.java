package me.gingerninja.authenticator.data.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.databinding.LabelListItemColorBinding;
import me.gingerninja.authenticator.ui.label.LabelListItemViewModel;

public class LabelListIteratorAdapter extends BaseIteratorAdapter<BindingViewHolder, Label> implements LabelListItemViewModel.LabelMenuItemClickListener {
    private LabelListItemViewModel.LabelMenuItemClickListener menuItemClickListener;

    private boolean dragEnabled = false;
    private int moveFrom = -1, moveTo = -1;

    public LabelListIteratorAdapter() {
        setHasStableIds(true);
    }

    @Override
    protected long getItemId(Label item) {
        return item.getId();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(getAdjustedPosition(position));
    }

    @Override
    public Label getItem(int position) {
        return super.getItem(getAdjustedPosition(position));
    }

    @NonNull
    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingViewHolder<>(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.label_list_item_color, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        LabelListItemColorBinding binding = (LabelListItemColorBinding) holder.getBinding();
        Label label = getItem(getAdjustedPosition(position));

        LabelListItemViewModel oldViewModel = binding.getViewModel();

        if (oldViewModel != null) {
            Label oldLabel = oldViewModel.getLabel();
            if (label.equals(oldLabel)) {
                oldViewModel.setMode(dragEnabled ? LabelListItemViewModel.Mode.DRAG : LabelListItemViewModel.Mode.IDLE);
                return;
            }
        }

        LabelListItemViewModel viewModel = new LabelListItemViewModel(label, holder.itemView);
        viewModel.setMenuItemClickListener(this);
        viewModel.setMode(dragEnabled ? LabelListItemViewModel.Mode.DRAG : LabelListItemViewModel.Mode.IDLE);

        binding.setViewModel(viewModel);
    }

    public void setMenuItemClickListener(LabelListItemViewModel.LabelMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    @Override
    public void onLabelMenuItemClicked(MenuItem item, Label label) {
        if (menuItemClickListener != null) {
            menuItemClickListener.onLabelMenuItemClicked(item, label);
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

            /*int originalPadding = cardView.getResources().getDimensionPixelSize(R.dimen.account_list_card_padding_normal);
            int draggingPadding = cardView.getResources().getDimensionPixelSize(R.dimen.account_list_card_padding_dragging);

            int targetPadding = isDragging ? draggingPadding : originalPadding;
            int extTargetPadding = isDragging ? originalPadding - draggingPadding : 0;
            //int originalPadding = cardView.getResources().getDimensionPixelSize(R.dimen.account_list_card_padding_normal);

            ValueAnimator paddingAnimator = ValueAnimator.ofInt(viewHolder.itemView.getPaddingStart(), targetPadding);
            paddingAnimator.addUpdateListener(valueAnimator -> {
                int p = (int) valueAnimator.getAnimatedValue();
                viewHolder.itemView.setPaddingRelative(p, p, p, p);
            });

            ValueAnimator paddingAnimator2 = ValueAnimator.ofInt(cardView.getContentPaddingTop(), extTargetPadding);
            paddingAnimator2.addUpdateListener(valueAnimator -> {
                int p = (int) valueAnimator.getAnimatedValue();
                cardView.setContentPadding(p, p, p, p);
            });

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animator, paddingAnimator, paddingAnimator2);
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
}
