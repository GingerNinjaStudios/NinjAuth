package me.gingerninja.authenticator.core.database

import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NinjAuthDaos @Inject constructor(
    private val dbAuthenticator: NinjAuthDatabaseAuthenticator
) {
    val accountDao = dbAuthenticator.database
        .map {
            it?.accountDao()
        }

    val labelDao = dbAuthenticator.database
        .map {
            it?.labelDao()
        }

    val accountLabelDao = dbAuthenticator.database
        .map {
            it?.accountLabelDao()
        }
}