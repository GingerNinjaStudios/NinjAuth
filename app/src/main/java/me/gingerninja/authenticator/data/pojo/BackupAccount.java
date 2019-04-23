package me.gingerninja.authenticator.data.pojo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Iterator;
import java.util.Set;

import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.db.entity.TempAccount;

public class BackupAccount {
    @SerializedName("uid")
    @Expose
    private String uid;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("source")
    @Expose
    private String source;

    @SerializedName("accountName")
    @Expose
    private String accountName;

    @SerializedName("issuer")
    @Expose
    private String issuer;

    @SerializedName("secret")
    @Expose
    private String secret;

    @SerializedName("algorithm")
    @Expose
    private String algorithm;

    @SerializedName("digits")
    @Expose
    private int digits = 6;

    @SerializedName("typeSpecificData")
    @Expose
    private long typeSpecificData;

    @SerializedName("labels")
    @Expose
    private String[] labelIds;

    @SerializedName("position")
    @Expose
    private int position = 0;

    @NonNull
    public static BackupAccount fromEntity(@NonNull Account account) {
        BackupAccount backupAccount = new BackupAccount();
        backupAccount.uid = account.getUid();
        backupAccount.title = account.getTitle();
        backupAccount.type = account.getType();
        backupAccount.source = account.getSource();
        backupAccount.accountName = account.getAccountName();
        backupAccount.issuer = account.getIssuer();
        backupAccount.secret = account.getSecret();
        backupAccount.algorithm = account.getAlgorithm();
        backupAccount.digits = account.getDigits();
        backupAccount.typeSpecificData = account.getTypeSpecificData();
        backupAccount.position = account.getPosition();

        Set<Label> labels = account.getLabels();
        if (labels != null && !labels.isEmpty()) {
            final int n = labels.size();
            Iterator<Label> iterator = labels.iterator();
            backupAccount.labelIds = new String[n];

            for (int i = 0; iterator.hasNext(); i++) {
                backupAccount.labelIds[i] = iterator.next().getUid();
            }
        }

        return backupAccount;
    }

    @NonNull
    public TempAccount toEntity() {
        TempAccount account = new TempAccount();

        account
                .setUid(uid)
                .setTitle(title)
                .setType(type)
                .setSource(source)
                .setAccountName(accountName)
                .setIssuer(issuer)
                .setSecret(secret)
                .setAlgorithm(algorithm)
                .setDigits(digits)
                .setTypeSpecificData(typeSpecificData)
                .setPosition(position);

        return account;
    }

    @Nullable
    public String[] getLabelIds() {
        return labelIds;
    }
}
