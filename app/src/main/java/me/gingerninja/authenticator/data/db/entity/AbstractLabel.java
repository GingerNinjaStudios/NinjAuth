package me.gingerninja.authenticator.data.db.entity;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

@Entity
abstract class AbstractLabel {
    @Key
    @Generated
    long id;

    String name;

    int color;
}
