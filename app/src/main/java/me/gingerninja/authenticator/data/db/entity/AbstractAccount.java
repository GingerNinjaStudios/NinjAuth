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

    String type;

    String source;

    @Column(nullable = false)
    String accountName;

    @Column(nullable = false)
    String secret;

    String issuer;

    @Column(value = ALGO_SHA1, nullable = false)
    String algorithm = ALGO_SHA1;

    @Column(value = "6")
    int digits = 6;

    long typeSpecificData;

    @ManyToMany
    @JunctionTable(type = AbstractAccountHasLabel.class)
    Set<AbstractLabel> labels;

    @Column(value = "0")
    int position = 0;

}
