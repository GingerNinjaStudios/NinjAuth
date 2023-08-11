package me.gingerninja.authenticator.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.collections.immutable.toImmutableSet
import me.gingerninja.authenticator.model.Account
import me.gingerninja.authenticator.model.HotpAccount
import me.gingerninja.authenticator.model.TotpAccount

@Entity(
    tableName = "Account",
    indices = [
        Index("uid", unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "accountName")
    val accountName: String,

    @ColumnInfo(name = "secret")
    val secret: String,

    @ColumnInfo(name = "digits", defaultValue = "6")
    val digits: Int = 6,

    @ColumnInfo(name = "type", defaultValue = "totp")
    val type: Type = Type.TOTP,

    @ColumnInfo(name = "source", defaultValue = "manual")
    val source: Source = Source.MANUAL,

    @ColumnInfo(name = "algorithm", defaultValue = "sha1")
    val algorithm: Algorithm = Algorithm.SHA1,

    // TODO val labels: ImmutableSet<Label>,

    @ColumnInfo(name = "typeSpecificData", defaultValue = "0")
    val typeSpecificData: Long = 0,

    @ColumnInfo(name = "title", defaultValue = "null")
    val title: String? = null,

    @ColumnInfo(name = "issuer", defaultValue = "null")
    val issuer: String? = null,

    @ColumnInfo(name = "position", defaultValue = "-1")
    val position: Int = -1,
) {
    enum class Type(val value: String) {
        HOTP("hotp"), TOTP("totp")
    }

    enum class Source(val value: String) {
        URI("uri"), MANUAL("manual")
    }

    enum class Algorithm(val value: String) {
        SHA1("sha1"), SHA256("sha256"), SHA512("sha512")
    }
}


fun AccountEntity.asModel() {
    when (type) {
        AccountEntity.Type.HOTP -> HotpAccount(
            id = id,
            uid = uid,
            accountName = accountName,
            secret = secret,
            digits = digits,
            source = source.asModel(),
            algorithm = algorithm.asModel(),
            counter = typeSpecificData,
            title = title,
            issuer = issuer,
            position = position,
            // labels are intentionally empty; AccountWithLabels is used for that
        )

        AccountEntity.Type.TOTP -> TotpAccount(
            id = id,
            uid = uid,
            accountName = accountName,
            secret = secret,
            digits = digits,
            source = source.asModel(),
            algorithm = algorithm.asModel(),
            period = typeSpecificData,
            title = title,
            issuer = issuer,
            position = position,
            // labels are intentionally empty; AccountWithLabels is used for that
        )
    }
}

fun AccountWithLabels.asModel() {
    with(account) {
        when (type) {
            AccountEntity.Type.HOTP -> HotpAccount(
                id = id,
                uid = uid,
                accountName = accountName,
                secret = secret,
                digits = digits,
                source = source.asModel(),
                algorithm = algorithm.asModel(),
                counter = typeSpecificData,
                title = title,
                issuer = issuer,
                position = position,
                labels = labels.map { it.asModel() }.toImmutableSet(),
            )

            AccountEntity.Type.TOTP -> TotpAccount(
                id = id,
                uid = uid,
                accountName = accountName,
                secret = secret,
                digits = digits,
                source = source.asModel(),
                algorithm = algorithm.asModel(),
                period = typeSpecificData,
                title = title,
                issuer = issuer,
                position = position,
                labels = labels.map { it.asModel() }.toImmutableSet(),
            )
        }
    }
}

fun AccountEntity.Source.asModel() = when (this) {
    AccountEntity.Source.URI -> Account.Source.URI
    AccountEntity.Source.MANUAL -> Account.Source.MANUAL
}

fun AccountEntity.Algorithm.asModel() = when (this) {
    AccountEntity.Algorithm.SHA1 -> Account.Algorithm.SHA1
    AccountEntity.Algorithm.SHA256 -> Account.Algorithm.SHA256
    AccountEntity.Algorithm.SHA512 -> Account.Algorithm.SHA512
}