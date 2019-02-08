package me.gingerninja.authenticator.data.db.wrapper;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Set;

import androidx.annotation.NonNull;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;

public class LabelWrapper extends Label implements Comparable<LabelWrapper> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ICON = "icon";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_POSITION = "pos";

    @Expose
    @SerializedName(FIELD_ID)
    private long id;

    @Expose
    @SerializedName(FIELD_NAME)
    private String name;

    @Expose
    @SerializedName(FIELD_COLOR)
    private int color;

    @Expose
    @SerializedName(FIELD_ICON)
    private String icon;

    @Expose
    @SerializedName(FIELD_POSITION)
    private int position;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public LabelWrapper setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LabelWrapper setColor(int color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LabelWrapper setIcon(String icon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LabelWrapper setPosition(int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Account> getAccounts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof LabelWrapper) {
            return getId() == ((LabelWrapper) obj).getId();
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(@NonNull LabelWrapper o) {
        return this.position - o.position;
    }
}
