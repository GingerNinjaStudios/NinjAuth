package me.gingerninja.authenticator.data.db.entity;

import android.os.Parcel;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Nullable;
import io.requery.PreInsert;
import io.requery.PropertyNameStyle;
import io.requery.Superclass;
import me.gingerninja.authenticator.R;

@Superclass
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractLabel {
    @Key
    @Generated
    long id;

    @Column(unique = true, nullable = false)
    String uid;

    String name;

    @Nullable
    String icon;

    int color;

    @Column(value = "-1")
    int position = -1;

    @ManyToMany
    Set<Account> accounts;

    @PreInsert
    void generateUID() {
        if (uid != null) {
            return;
        }

        generateUID(null);
    }

    public void generateUID(@androidx.annotation.Nullable byte[] random) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            digest.update(name.getBytes(StandardCharsets.UTF_8));
            if (icon != null) {
                digest.update(icon.getBytes(StandardCharsets.UTF_8));
            }

            ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
            buffer.putInt(color);

            digest.update(buffer);

            if (random != null) {
                digest.update(random);
            }


            uid = Base64.encodeToString(digest.digest(), Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Parcel writeToParcel() {
        Parcel dest = Parcel.obtain();

        dest.writeLong(id);
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeInt(color);
        dest.writeInt(position);

        return dest;
    }

    public static void restoreFromParcel(@NonNull Label label, @NonNull Parcel parcel) {
        parcel.setDataPosition(0);

        label.id = parcel.readInt();
        label.setUid(parcel.readString());
        label.setName(parcel.readString());
        label.setIcon(parcel.readString());
        label.setColor(parcel.readInt());
        label.setPosition(parcel.readInt());
    }

    @DrawableRes
    public int getIconResourceId() {
        return getIconResourceId(icon);
    }

    @DrawableRes
    public static int getIconResourceId(String icon) {
        if (icon == null) {
            return 0;
        }

        switch (icon) {
            case "work":
                return R.drawable.label_icon_work;
            default:
                return 0;
        }
    }
}
