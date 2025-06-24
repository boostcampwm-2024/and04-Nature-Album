package com.and04.naturealbum.ui.maps

sealed interface PreloadState {
    data object Loading : PreloadState

    data object Success : PreloadState

    data object Fail : PreloadState
}
