package me.gingerninja.authenticator.data.db.entity;

import java.util.Set;

import io.requery.CascadeAction;
import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.OneToMany;

@Entity
abstract class AbstractCategory {
    @Key
    @Generated
    long id;

    String name;

    int color;

    @Column(value = "0")
    int position;

    @OneToMany
    Set<Account> accounts;
}
