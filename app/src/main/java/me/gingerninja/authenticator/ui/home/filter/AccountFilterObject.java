package me.gingerninja.authenticator.ui.home.filter;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Set;

import me.gingerninja.authenticator.data.db.entity.Label;

public class AccountFilterObject {
    @Nullable
    private String searchString;

    private boolean matchAllLabels;

    @Nullable
    private Set<Label> labels;

    @Nullable
    public Set<Label> getLabels() {
        return labels;
    }

    public boolean isMatchAllLabels() {
        return matchAllLabels;
    }

    public boolean hasLabels() {
        return labels != null && !labels.isEmpty();
    }

    @Nullable
    public String getSearchString() {
        return getSearchString(true);
    }

    @Nullable
    public String getSearchString(boolean escape) {
        if (searchString == null) {
            return null;
        } else {
            if (escape) {
                return "%" +
                        searchString
                                .toLowerCase()
                                .replaceAll("%", "\\\\%")
                                .replaceAll("_", "\\\\_") +
                        "%";
            } else {
                return searchString;
            }
        }
    }

    public boolean hasSearchString() {
        return searchString != null && !searchString.isEmpty();
    }

    public boolean hasFilter() {
        return hasLabels() || hasSearchString();
    }

    public static class Builder {
        AccountFilterObject object;

        public Builder() {
            object = new AccountFilterObject();
        }

        public Builder setMatchAllLabels(boolean matchAllLabels) {
            object.matchAllLabels = matchAllLabels;
            return this;
        }

        public Builder setLabels(@Nullable Set<Label> labels) {
            if (labels != null && labels.isEmpty()) {
                labels = null;
            }

            object.labels = labels;
            return this;
        }

        public Builder setSearchString(@Nullable String text) {
            text = text != null ? text.trim() : null;

            if (TextUtils.isEmpty(text)) {
                object.searchString = null;
            } else {
                object.searchString = text;
            }
            return this;
        }

        public AccountFilterObject build() {
            return object;
        }
    }
}
