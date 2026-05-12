package me.gingerninja.authenticator.feature.account

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import me.gingerninja.authenticator.core.codegen.CodeGenerator
import me.gingerninja.authenticator.core.codegen.OtpGenerator
import me.gingerninja.authenticator.core.data.repository.AccountsRepository
import me.gingerninja.authenticator.core.model.Account
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountsRepository: AccountsRepository,
    internal val codeGenerator: CodeGenerator,
) : ViewModel() {
    private val filters = combine(
        savedStateHandle.getStateFlow<String?>(SAVED_STATE_KEY_SEARCH, null)
            .debounce(200.milliseconds)
            .distinctUntilChanged(),
        savedStateHandle.getStateFlow<LongArray?>(SAVED_STATE_KEY_LABELS, null),
    ) { search, labels ->
        AccountsFilters(
            search = search?.takeIf { it.isNotBlank() },
            labels = labels?.toList()?.takeIf { it.isNotEmpty() },
        )
    }

    private val filteredAccountList = filters.flatMapLatest { (search, labels) ->
        accountsRepository.getAccounts(
            search = search,
            labels = labels,
        )
    }

    val state: StateFlow<AccountsUiState> = combine(
        filteredAccountList,
        filters
    ) { accounts, filters ->
        AccountsUiState(
            isLoading = false,
            accounts = accounts,
            filters = filters,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AccountsUiState()
        )

    internal fun search(term: String) {
        savedStateHandle[SAVED_STATE_KEY_SEARCH] = term
    }

    companion object {
        private const val SAVED_STATE_KEY_SEARCH = "search"
        private const val SAVED_STATE_KEY_LABELS = "labels"
    }
}

data class AccountsUiState(
    val isLoading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val filters: AccountsFilters = AccountsFilters(),
)

data class AccountsFilters(
    val search: String? = null,
    val labels: List<Long>? = null,
)