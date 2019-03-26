package me.gingerninja.authenticator.data.db.entity;

import java.util.Set;

import io.requery.Column;
import io.requery.Entity;
import io.requery.JunctionTable;
import io.requery.ManyToMany;
import io.requery.PropertyNameStyle;
import io.requery.Table;

@Table(createAttributes = {"TEMP"}, name = "Account")
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractTempAccount extends AbstractAccount {
    public static final int MODE_INSERT = 0;
    public static final int MODE_UPDATE = 1;

    @Column(value = "false", nullable = false)
    boolean restoreExists;

    @Column(value = "" + MODE_INSERT, nullable = false)
    int restoreMode = MODE_INSERT;

    @Column(value = "true", nullable = false)
    boolean restore = true;

    @ManyToMany
    @JunctionTable(type = AbstractTempAccountHasLabel.class)
    Set<TempLabel> labels;
}
