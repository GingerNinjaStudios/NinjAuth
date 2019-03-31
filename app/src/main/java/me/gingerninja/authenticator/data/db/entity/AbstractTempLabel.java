package me.gingerninja.authenticator.data.db.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import androidx.annotation.IntDef;
import androidx.core.util.ObjectsCompat;
import io.requery.Column;
import io.requery.Entity;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Nullable;
import io.requery.PropertyNameStyle;
import io.requery.Table;

@Table(/*createAttributes = {"TEMP"}, */name = "TempLabel")
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN, cacheable = false)
abstract class AbstractTempLabel extends AbstractLabel {
    @IntDef({RestoreMode.INSERT, RestoreMode.UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RestoreMode {
        int INSERT = 0;
        int UPDATE = 1;
    }

    @Key
    Long id;

    /**
     * Defines whether the label already has a matching pair in the normal database. This can help
     * setup the UI so the user can select to replace the existing or insert a new label.
     */
    @Nullable
    String restoreMatchingUid = null;

    @RestoreMode
    @Column(value = "" + RestoreMode.INSERT, nullable = false)
    int restoreMode = RestoreMode.INSERT;

    @Column(value = "true", nullable = false)
    boolean restore = true;

    @ManyToMany
    Set<TempAccount> accounts;

    public boolean equalsToLabel(Label label) {
        return ObjectsCompat.equals(this.name, label.getName()) &&
                this.color == label.getColor() &&
                ObjectsCompat.equals(this.icon, label.getIcon());
    }
}
