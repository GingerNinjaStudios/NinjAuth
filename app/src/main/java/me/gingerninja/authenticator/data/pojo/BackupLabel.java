package me.gingerninja.authenticator.data.pojo;

import android.graphics.Color;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import me.gingerninja.authenticator.data.db.entity.Label;

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

    @SerializedName("position")
    @Expose
    private int position;

    @NonNull
    public static BackupLabel fromEntity(@NonNull Label label) {
        BackupLabel backupLabel = new BackupLabel();

        backupLabel.uid = label.getUid();
        backupLabel.name = label.getName();
        backupLabel.color = String.format("#%06X", 0xFFFFFF & label.getColor());
        backupLabel.position = label.getPosition();

        return backupLabel;
    }

    @NonNull
    public Label toEntity() {
        Label label = new Label();

        label
                .setUid(uid)
                .setName(name)
                .setColor(Color.parseColor(color))
                .setPosition(position);

        return label;
    }
}
