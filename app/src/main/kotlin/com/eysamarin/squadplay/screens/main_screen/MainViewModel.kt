package com.eysamarin.squadplay.screens.main_screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState
}

data class MainUIState(
    val body: String = "STUB_BODY",
    val label: String = "STUB_LABEL"
)