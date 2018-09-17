package me.gingerninja.authenticator.data.db.entity;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.Nullable;

@Entity
abstract class AbstractAccount {
    public static final String TYPE_TOTP = "totp";
    public static final String TYPE_HOTP = "hotp";

    public static final String ALGO_SHA1 = "sha1";
    public static final String ALGO_SHA256 = "sha256";
    public static final String ALGO_SHA512 = "sha512";

    @Key
    @Generated
    long id;

    /**
     * Custom title for the code. Can be null.
     */
    @Nullable
    String title;

    String type;

    @Column(nullable = false)
    String accountName;

    @Column(nullable = false)
    String secret;

    String issuer;

    @Column(value = ALGO_SHA1, nullable = false)
    String algorithm;

    @Column(value = "6")
    int digits;

    @Column(value = "0")
    long counter;

    @Column(value = "30")
    long period;
}
