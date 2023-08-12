package me.gingerninja.authenticator.core.database.model

import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.collections.immutable.toImmutableSet
import me.gingerninja.authenticator.core.model.Account
import me.gingerninja.authenticator.core.model.HotpAccount
import me.gingerninja.authenticator.core.model.TotpAccount
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.random.Random

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

    @ColumnInfo(name = "uid")
    val uid: String = generateAccountUID(
        accountName = accountName,
        source = source,
        secret = secret,
        algorithm = algorithm,
        type = type,
        typeSpecificData = typeSpecificData,
        digits = digits
    ),
) {
    fun withNewUid() = copy(uid = generateUID(Random.nextBytes(8)))

    private fun generateUID(
        random: ByteArray? = null,
        accountName: String = this.accountName,
        source: Source = this.source,
        secret: String = this.secret,
        algorithm: Algorithm = this.algorithm,
        type: Type = this.type,
        typeSpecificData: Long = this.typeSpecificData,
        digits: Int = this.digits
    ) = generateAccountUID(
        random,
        accountName,
        source,
        secret,
        algorithm,
        type,
        typeSpecificData,
        digits
    )

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

private fun generateAccountUID(
    random: ByteArray? = null,
    accountName: String,
    source: AccountEntity.Source,
    secret: String,
    algorithm: AccountEntity.Algorithm,
    type: AccountEntity.Type,
    typeSpecificData: Long,
    digits: Int
): String {
    val digest = MessageDigest.getInstance("SHA-384")
    digest.update(accountName.toByteArray(StandardCharsets.UTF_8))
    digest.update(source.value.toByteArray(StandardCharsets.UTF_8))
    digest.update(secret.toByteArray(StandardCharsets.UTF_8))
    digest.update(algorithm.value.toByteArray(StandardCharsets.UTF_8))
    digest.update(type.value.toByteArray(StandardCharsets.UTF_8))
    val buffer = ByteBuffer.allocate(Long.SIZE_BYTES + Int.SIZE_BYTES).apply {
        putLong(typeSpecificData)
        putInt(digits)
    }
    digest.update(buffer)

    if (random != null) {
        digest.update(random)
    }

    return Base64.encodeToString(digest.digest(), Base64.NO_WRAP)
}