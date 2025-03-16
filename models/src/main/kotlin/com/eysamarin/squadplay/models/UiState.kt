package com.eysamarin.squadplay.models

sealed interface UiState<out T> {
    object Empty : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Normal<T>(val data: T) : UiState<T>
    data class Error(val description: String) : UiState<Nothing>
}