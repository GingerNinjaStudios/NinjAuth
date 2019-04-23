package me.gingerninja.authenticator.data.db.entity;

import androidx.annotation.IntDef;
import androidx.core.util.ObjectsCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import io.requery.Column;
import io.requery.Entity;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Nullable;
import io.requery.PropertyNameStyle;
import io.requery.Table;

@Table(/*createAttributes = {"TEMP"}, */name = "TempAccount")
@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN, cacheable = false)
abstract class AbstractTempAccount extends AbstractAccount {
    @Key
    Long id;
    /**
     * Defines whether the account already has a matching pair in the normal database. This can help
     * setup the UI so the user can select to replace the existing or insert a new account.
     */
    @Nullable
    String restoreMatchingUid = null;
    @RestoreMode
    @Column(value = "" + RestoreMode.INSERT, nullable = false)
    int restoreMode = RestoreMode.INSERT;
    @Column(value = "true", nullable = false)
    boolean restore = true;
    @ManyToMany
    @JunctionTable(type = AbstractTempAccountHasLabel.class)
    Set<TempLabel> labels;

    public boolean equalsToAccount(Account account) {
        return ObjectsCompat.equals(this.accountName, account.getAccountName()) &&
                ObjectsCompat.equals(this.title, account.getTitle()) &&
                ObjectsCompat.equals(this.issuer, account.getIssuer()) &&
                ObjectsCompat.equals(this.secret, account.getSecret()) &&
                ObjectsCompat.equals(this.type, account.getType()) &&
                ObjectsCompat.equals(this.algorithm, account.getAlgorithm()) &&
                this.digits == account.getDigits();
    }

    @IntDef({RestoreMode.INSERT, RestoreMode.UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RestoreMode {
        int INSERT = 0;
        int UPDATE = 1;
    }
}
