package com.and04.naturealbum.ui.add.savephoto.contract

sealed interface SavePhotoIntent {
    data object BackButtonClicked : SavePhotoIntent
    data object CancelButtonClicked : SavePhotoIntent
    data object SaveButtonClicked : SavePhotoIntent
    data object LabelSelectClicked : SavePhotoIntent
    data object MyPageButtonClicked : SavePhotoIntent
    data class DescriptionInput(val text: String) : SavePhotoIntent
    data object RepresentedToggleClicked : SavePhotoIntent
}
