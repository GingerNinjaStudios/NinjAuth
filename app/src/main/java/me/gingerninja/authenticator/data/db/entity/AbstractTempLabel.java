package me.gingerninja.authenticator.data.db.entity;

import io.requery.Entity;
import io.requery.PropertyNameStyle;
import io.requery.Table;

@Table(createAttributes = {"TEMP"}, name = "Label")
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
abstract class AbstractTempLabel extends AbstractLabel {
}
