package me.gingerninja.authenticator.core.data.repository

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.gingerninja.authenticator.core.database.NinjAuthDaos
import me.gingerninja.authenticator.core.database.model.asModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountsRepository @Inject constructor(
    private val daos: NinjAuthDaos
) {
    fun getAccounts(
        search: String? = null,
        labels: List<Long>? = null,
        matchAllLabels: Boolean = false,
    ) = daos.accountDao
        .flatMapLatest {
            it?.getAccounts(
                search = search,
                labels = labels,
                matchAllLabels = matchAllLabels,
            ) ?: emptyFlow()
        }
        .map { accountList ->
            accountList.map { it.asModel() }
        }
}