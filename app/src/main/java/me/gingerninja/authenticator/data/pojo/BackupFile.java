package me.gingerninja.authenticator.data.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BackupFile {
    @Expose
    @SerializedName("accounts")
    private List<BackupAccount> accounts;

    @Expose
    @SerializedName("labels")
    private List<BackupLabel> labels;

    public List<BackupAccount> getAccounts() {
        return accounts;
    }

    public BackupFile setAccounts(List<BackupAccount> accounts) {
        this.accounts = accounts;
        return this;
    }

    public List<BackupLabel> getLabels() {
        return labels;
    }

    public BackupFile setLabels(List<BackupLabel> labels) {
        this.labels = labels;
        return this;
    }
}
