package me.gingerninja.authenticator.data.db.entity;

import java.util.Set;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Nullable;
import io.requery.PropertyNameStyle;

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

}
