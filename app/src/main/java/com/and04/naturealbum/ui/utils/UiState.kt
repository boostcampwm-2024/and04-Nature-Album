package com.and04.naturealbum.ui.utils

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error<T>(val message: T) : UiState<Nothing>()
}

sealed interface UiStatus {
    data object Idle : UiStatus
    data object Loading : UiStatus
    data object Success : UiStatus
}
