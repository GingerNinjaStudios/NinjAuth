package me.gingerninja.authenticator.data.db.entity;

import android.os.Parcel;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import androidx.annotation.NonNull;
import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Nullable;
import io.requery.PreInsert;
import io.requery.PropertyNameStyle;
import io.requery.Superclass;

@Superclass
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractAccount {
    public static final long DEFAULT_COUNTER = 0;
    public static final long DEFAULT_PERIOD = 30;

    public static final String TYPE_TOTP = "totp";
    public static final String TYPE_HOTP = "hotp";

    public static final String ALGO_SHA1 = "sha1";
    public static final String ALGO_SHA256 = "sha256";
    public static final String ALGO_SHA512 = "sha512";

    public static final String SOURCE_URI = "uri";
    public static final String SOURCE_MANUAL = "manual";

    @Key
    @Generated
    long id;

    @Column(unique = true, nullable = false)
    String uid;

    /**
     * Custom title for the code. Can be null.
     */
    @Nullable
    String title;

    @Column(nullable = false, value = TYPE_TOTP)
    String type = TYPE_TOTP;

    @Column(nullable = false, value = SOURCE_MANUAL)
    String source = SOURCE_MANUAL;

    @Column(nullable = false)
    String accountName;

    @Column(nullable = false)
    String secret;

    @Nullable
    String issuer;

    @Column(value = ALGO_SHA1, nullable = false)
    String algorithm = ALGO_SHA1;

    @Column(value = "6", nullable = false)
    int digits = 6;

    @Column(value = "0", nullable = false)
    long typeSpecificData;

    @ManyToMany
    @JunctionTable(type = AbstractAccountHasLabel.class)
    Set<Label> labels;

    @Column(value = "-1", nullable = false)
    int position = -1;

    @NonNull
    public Parcel writeToParcel() {
        Parcel dest = Parcel.obtain();

        dest.writeLong(id);
        dest.writeString(uid);
        dest.writeString(title);
        dest.writeString(type);
        dest.writeString(source);
        dest.writeString(accountName);
        dest.writeString(secret);
        dest.writeString(issuer);
        dest.writeString(algorithm);
        dest.writeInt(digits);
        dest.writeLong(typeSpecificData);
        dest.writeInt(position);

        return dest;
    }

    public static void restoreFromParcel(@NonNull Account account, @NonNull Parcel parcel) {
        parcel.setDataPosition(0);

        account.id = parcel.readInt();
        account.setUid(parcel.readString());
        account.setTitle(parcel.readString());
        account.setType(parcel.readString());
        account.setSource(parcel.readString());
        account.setAccountName(parcel.readString());
        account.setSecret(parcel.readString());
        account.setIssuer(parcel.readString());
        account.setAlgorithm(parcel.readString());
        account.setDigits(parcel.readInt());
        account.setTypeSpecificData(parcel.readLong());
        account.setPosition(parcel.readInt());
    }

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
            digest.update(accountName.getBytes(StandardCharsets.UTF_8));
            digest.update(source.getBytes(StandardCharsets.UTF_8));
            digest.update(secret.getBytes(StandardCharsets.UTF_8));
            digest.update(algorithm.getBytes(StandardCharsets.UTF_8));
            digest.update(type.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate((Long.SIZE + Integer.SIZE) / Byte.SIZE);
            buffer.putLong(typeSpecificData);
            buffer.putInt(digits);

            digest.update(buffer);

            if (random != null) {
                digest.update(random);
            }


            uid = Base64.encodeToString(digest.digest(), Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /*public void generateUID(boolean time) {
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
    }*/
}
