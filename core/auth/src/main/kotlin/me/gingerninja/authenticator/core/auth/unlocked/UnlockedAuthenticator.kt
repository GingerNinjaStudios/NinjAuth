package me.gingerninja.authenticator.core.auth.unlocked

import me.gingerninja.authenticator.core.auth.Authenticator
import me.gingerninja.authenticator.core.auth.password.PasswordAuthenticator
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnlockedAuthenticator @Inject constructor(
    private val settings: NinjAuthSettings,
    private val dbAuthenticator: NinjAuthDatabaseAuthenticator,
) : Authenticator<Unit>(
    settings,
    dbAuthenticator
) {

    suspend fun authenticate() {
        authenticate(Unit)
    }

    override suspend fun authenticate(config: Unit) {
        dbAuthenticator.openDatabase(PasswordAuthenticator.defaultPassBytes)
    }
}