package com.eysamarin.squadplay.models

sealed interface UiState<out T> {
    object Empty : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Normal<T>(val data: T) : UiState<T>
    data class Error(val description: String) : UiState<Nothing>
}

suspend fun <INPUT_TYPE, OUTPUT_TYPE> UiState<INPUT_TYPE>.suspendMap(
    mapper: suspend (INPUT_TYPE) -> OUTPUT_TYPE,
): UiState<OUTPUT_TYPE> = when (this) {
    UiState.Empty -> UiState.Empty
    is UiState.Error -> UiState.Error(this.description)
    UiState.Loading -> UiState.Loading
    is UiState.Normal<INPUT_TYPE> -> UiState.Normal(mapper(this.data))
}