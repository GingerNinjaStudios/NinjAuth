package me.gingerninja.authenticator.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.getValue

abstract class BaseViewModel<S: UiState, A: UiAction, E: UiEffect> : ViewModel() {
    private val _effect: Channel<E> = Channel()
    val effect = _effect.receiveAsFlow()

    private val _state: MutableStateFlow<S> by lazy { MutableStateFlow(createInitialScreenState()) }
    val state by lazy { _state.asStateFlow() }

    val nice: StateFlow<S>
        field = MutableStateFlow(createInitialScreenState())


    protected abstract fun createInitialScreenState(): S
}

interface BaseState<S> {
    val state: StateFlow<S>
}

class Nice: ViewModel(), BaseState<Any> {
    override val state: StateFlow<Any> = combine(flowOf(""), flowOf("")){
        ""
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

}

interface UiState
interface UiAction
interface UiEffect