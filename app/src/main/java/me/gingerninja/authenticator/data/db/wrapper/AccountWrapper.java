package me.gingerninja.authenticator.data.db.wrapper;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import io.requery.Nullable;
import io.requery.query.Tuple;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;

public class AccountWrapper extends Account {
    public static final String TUPLE_KEY_LABELS = "labels";

    private final Gson gson;
    private final Tuple tuple;

    private Set<Label> labels;

    private AccountWrapper(Gson gson, Tuple tuple) {
        this.gson = gson;
        this.tuple = tuple;
    }

    public static String getType(Tuple tuple) {
        return tuple.get(Account.TYPE);
    }

    @Override
    public String getTitle() {
        return tuple.get(Account.TITLE);
    }

    @Override
    public String getIssuer() {
        return tuple.get(Account.ISSUER);
    }

    @Override
    public long getId() {
        return tuple.get(Account.ID);
    }

    @Override
    @Nullable
    public Set<Label> getLabels() {
        if (labels != null) {
            return labels;
        }

        String rawStr = tuple.get(TUPLE_KEY_LABELS);

        if (rawStr == null) {
            return labels = Collections.emptySet();
        }

        rawStr = "[" + rawStr + "]";

        LabelWrapper[] labelWrappers = gson.fromJson(rawStr, LabelWrapper[].class);

        labels = new TreeSet<>(Arrays.asList(labelWrappers));

        return labels;
    }

    @Override
    public String getType() {
        return tuple.get(Account.TYPE);
    }

    @Override
    public String getSource() {
        return tuple.get(Account.SOURCE);
    }

    @Override
    public String getAccountName() {
        return tuple.get(Account.ACCOUNT_NAME);
    }

    @Override
    public String getSecret() {
        return tuple.get(Account.SECRET);
    }

    @Override
    public String getAlgorithm() {
        return tuple.get(Account.ALGORITHM);
    }

    @Override
    public int getDigits() {
        return tuple.get(Account.DIGITS);
    }

    @Override
    public int getPosition() {
        return tuple.get(Account.POSITION);
    }

    @Override
    public long getTypeSpecificData() {
        return tuple.get(Account.TYPE_SPECIFIC_DATA);
    }

    @Override
    public Account setAccountName(String accountName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setAlgorithm(String algorithm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setDigits(int digits) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setIssuer(String issuer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setPosition(int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setSecret(String secret) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setSource(String source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setTitle(String title) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setType(String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account setTypeSpecificData(long typeSpecificData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof AccountWrapper) {
            return this.getId() == ((AccountWrapper) obj).getId();
        }

        return super.equals(obj);
    }

    public static class Factory {
        private final Gson gson;

        public Factory(Gson gson) {
            this.gson = gson;
        }

        public AccountWrapper create(Tuple tuple) {
            return new AccountWrapper(gson, tuple);
        }
    }
}
