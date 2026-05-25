package com.example.letterble.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvents: SharedFlow<HomeNavigationEvent> = _navigationEvents.asSharedFlow()

    fun onReceivedClicked() {
        emitNavigation(HomeNavigationEvent.NavigateToReceived)
    }

    fun onCarryClicked() {
        emitNavigation(HomeNavigationEvent.NavigateToCarry)
    }

    fun onCreateLetterClicked() {
        emitNavigation(HomeNavigationEvent.NavigateToEditLetter)
    }

    private fun emitNavigation(event: HomeNavigationEvent) {
        viewModelScope.launch {
            _navigationEvents.emit(event)
        }
    }
}

sealed interface HomeNavigationEvent {
    data object NavigateToReceived : HomeNavigationEvent
    data object NavigateToCarry : HomeNavigationEvent
    data object NavigateToEditLetter : HomeNavigationEvent
}
