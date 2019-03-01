package me.gingerninja.authenticator.data.db.entity;

import io.requery.Entity;
import io.requery.PropertyNameStyle;
import io.requery.Table;

@Table(createAttributes = {"TEMP"}, name = "Account")
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractTempAccount extends AbstractAccount {
}
