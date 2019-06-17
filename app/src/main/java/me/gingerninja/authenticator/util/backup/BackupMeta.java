package me.gingerninja.authenticator.util.backup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class BackupMeta {
    public static final int VERSION = 1;

    @SuppressWarnings("FieldCanBeLocal")
    @Expose
    @SerializedName("version")
    private int version = VERSION;

    @Expose
    @SerializedName("date")
    private Date date = new Date();

    @Expose
    @SerializedName("comment")
    private String comment;

    @Expose
    @SerializedName("isAutoBackup")
    private boolean isAutoBackup;

    private BackupMeta() {
    }

    private BackupMeta(String comment, boolean isAutoBackup) {
        this.comment = comment;
        this.isAutoBackup = isAutoBackup;
    }

    public int getVersion() {
        return version;
    }

    public Date getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    public boolean isAutoBackup() {
        return isAutoBackup;
    }

    public static class Builder {
        private String comment;
        private boolean isAutoBackup;

        public Builder() {
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder setAutoBackup(boolean autoBackup) {
            isAutoBackup = autoBackup;
            return this;
        }

        public BackupMeta build() {
            return new BackupMeta(comment, isAutoBackup);
        }
    }
}
