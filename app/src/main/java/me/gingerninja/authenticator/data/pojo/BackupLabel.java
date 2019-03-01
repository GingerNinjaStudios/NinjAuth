package me.gingerninja.authenticator.data.pojo;

import android.graphics.Color;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.db.entity.TempLabel;

public class BackupLabel {
    @SerializedName("uid")
    @Expose
    private String uid;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("color")
    @Expose
    private String color;

    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("position")
    @Expose
    private int position;

    @NonNull
    public static BackupLabel fromEntity(@NonNull Label label) {
        BackupLabel backupLabel = new BackupLabel();

        backupLabel.uid = label.getUid();
        backupLabel.name = label.getName();
        backupLabel.color = String.format("#%06X", 0xFFFFFF & label.getColor());
        backupLabel.icon = label.getIcon();
        backupLabel.position = label.getPosition();

        return backupLabel;
    }

    @NonNull
    public TempLabel toEntity() {
        TempLabel label = new TempLabel();

        label
                .setUid(uid)
                .setName(name)
                .setColor(Color.parseColor(color))
                .setIcon(icon)
                .setPosition(position);

        return label;
    }
}
