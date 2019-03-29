package me.gingerninja.authenticator.data.adapter;

import android.database.Cursor;

import java.sql.SQLException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.requery.sql.ResultSetIterator;

public abstract class BaseIteratorAdapter<VH extends RecyclerView.ViewHolder, ItemType> extends RecyclerView.Adapter<VH> {
    @Nullable
    protected ResultSetIterator<ItemType> iterator;

    public BaseIteratorAdapter() {
        setHasStableIds(true);
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

    public void setResults(@Nullable ResultSetIterator<ItemType> iterator) {
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

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        close();
    }

    public ItemType getItem(int position) {
        assert iterator != null;
        return iterator.get(position);
    }

    @Override
    public long getItemId(int position) {
        return iterator == null ? RecyclerView.NO_ID : getItemId(iterator.get(position));
    }

    protected abstract long getItemId(ItemType item);
}
