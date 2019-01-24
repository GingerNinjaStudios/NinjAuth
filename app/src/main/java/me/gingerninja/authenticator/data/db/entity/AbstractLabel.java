package me.gingerninja.authenticator.data.db.entity;

import java.util.Set;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Nullable;
import io.requery.PropertyNameStyle;

@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractLabel {
    @Key
    @Generated
    long id;

    String name;

    @Nullable
    String icon;

    int color;

    @Column(value = "0")
    int position;

    @ManyToMany
    Set<Account> accounts;
}
