package me.gingerninja.authenticator.core.database.util

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import me.gingerninja.authenticator.core.database.model.AccountEntity

class InstantConverter {
    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let(Instant::fromEpochMilliseconds)

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? = instant?.toEpochMilliseconds()
}

class AccountTypeConverter {
    @TypeConverter
    fun stringToAccountType(value: String): AccountEntity.Type = when (value) {
        AccountEntity.Type.HOTP.value -> AccountEntity.Type.HOTP
        AccountEntity.Type.TOTP.value -> AccountEntity.Type.TOTP
        else -> throw IllegalArgumentException("Invalid type: $value")
    }

    @TypeConverter
    fun accountTypeToString(source: AccountEntity.Type) = source.value
}

class AccountSourceConverter {
    @TypeConverter
    fun stringToAccountSource(value: String): AccountEntity.Source = when (value) {
        AccountEntity.Source.URI.value -> AccountEntity.Source.URI
        AccountEntity.Source.MANUAL.value -> AccountEntity.Source.MANUAL
        else -> throw IllegalArgumentException("Invalid source: $value")
    }

    @TypeConverter
    fun accountSourceToString(source: AccountEntity.Source) = source.value
}

class AccountAlgorithmConverter {
    @TypeConverter
    fun stringToAccountAlgorithm(value: String): AccountEntity.Algorithm = when (value) {
        AccountEntity.Algorithm.SHA1.value -> AccountEntity.Algorithm.SHA1
        AccountEntity.Algorithm.SHA256.value -> AccountEntity.Algorithm.SHA256
        AccountEntity.Algorithm.SHA512.value -> AccountEntity.Algorithm.SHA512
        else -> throw IllegalArgumentException("Invalid algorithm: $value")
    }

    @TypeConverter
    fun accountAlgorithmToString(source: AccountEntity.Algorithm) = source.value
}