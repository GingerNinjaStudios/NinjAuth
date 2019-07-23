package me.gingerninja.authenticator.data.db.wrapper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.Set;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;

public class AccountDataProjector {
    @NonNull
    private final Account account;

    public AccountDataProjector(@NonNull Account account) {
        this.account = account;
    }

    public String getTitle() {
        return account.getTitle();
    }

    public String getIssuer() {
        return account.getIssuer();
    }

    public long getId() {
        return account.getId();
    }

    public Set<Label> getLabels() {
        return account.getLabels();
    }

    public String getUid() {
        return account.getUid();
    }

    public String getTypeRaw() {
        return account.getType();
    }

    @StringRes
    public int getTypeAsResource() {
        switch (account.getType()) {
            case Account.TYPE_HOTP:
                return R.string.account_type_hotp_long;
            case Account.TYPE_TOTP:
                return R.string.account_type_totp_long;
        }

        return 0;
    }

    public String getSource() {
        return account.getSource();
    }

    public String getAccountName() {
        return account.getAccountName();
    }

    public String getSecret() {
        return account.getSecret();
    }

    public String getAlgorithmRaw() {
        return account.getAlgorithm();
    }

    public String getAlgorithmAsReadable() {
        switch (account.getAlgorithm()) {
            case Account.ALGO_SHA1:
                return "SHA-1";
            case Account.ALGO_SHA256:
                return "SHA-256";
            case Account.ALGO_SHA512:
                return "SHA-512";
        }

        return null;
    }

    public int getDigits() {
        return account.getDigits();
    }

    public long getTypeSpecificData() {
        return account.getTypeSpecificData();
    }

    public String getTypeSpecificDataAsReadable(@NonNull Context ctx) {
        switch (account.getType()) {
            case Account.TYPE_HOTP:
                return ctx.getString(R.string.account_type_spec_hotp_detail_full, account.getTypeSpecificData());
            case Account.TYPE_TOTP:
                return ctx.getString(R.string.account_type_spec_totp_detail_full, account.getTypeSpecificData());
        }

        return null;
    }

    public int getPosition() {
        return account.getPosition();
    }
}
