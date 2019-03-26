package me.gingerninja.authenticator.data.db.entity;

import java.util.Set;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ManyToMany;
import io.requery.PropertyNameStyle;
import io.requery.Table;

@Table(createAttributes = {"TEMP"}, name = "Label")
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractTempLabel extends AbstractLabel {
    public static final int MODE_INSERT = 0;
    public static final int MODE_UPDATE = 1;

    @Column(value = "false", nullable = false)
    boolean restoreExists;

    @Column(value = "" + MODE_INSERT, nullable = false)
    int restoreMode = MODE_INSERT;

    @Column(value = "true", nullable = false)
    boolean restore = true;

    @ManyToMany
    Set<TempAccount> accounts;
}
