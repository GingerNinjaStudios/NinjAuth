package me.gingerninja.authenticator.data.db.entity;

import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.PropertyNameStyle;

@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractAccountHasLabel {
    @Key
    @ForeignKey(references = AbstractAccount.class, referencedColumn = "id")
    @ManyToOne
    protected Account account;

    @Key
    @ForeignKey(references = AbstractLabel.class, referencedColumn = "id")
    @ManyToOne
    protected Label label;
}
