package me.gingerninja.authenticator.data.db.entity;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.PropertyNameStyle;
import io.requery.Table;

@Table(createAttributes = {"TEMP"}, name = "AccountHasLabel")
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractTempAccountHasLabel extends AbstractAccountHasLabel {
    @Key
    @ForeignKey(references = AbstractTempAccount.class, referencedColumn = "id")
    @ManyToOne
    protected TempAccount account;

    @Key
    @ForeignKey(references = AbstractTempLabel.class, referencedColumn = "id")
    @ManyToOne
    protected TempLabel label;
}
