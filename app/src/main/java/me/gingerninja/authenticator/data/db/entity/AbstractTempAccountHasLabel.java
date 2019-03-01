package me.gingerninja.authenticator.data.db.entity;

import io.requery.Entity;
import io.requery.PropertyNameStyle;
import io.requery.Table;

@Table(createAttributes = {"TEMP"}, name = "AccountHasLabel")
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractTempAccountHasLabel extends AbstractAccountHasLabel {
}
