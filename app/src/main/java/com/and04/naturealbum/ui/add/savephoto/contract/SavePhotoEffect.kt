package com.and04.naturealbum.ui.add.savephoto.contract

sealed interface SavePhotoEffect {
    sealed interface Navigation : SavePhotoEffect {
        data object Back : Navigation
        data object Cancel : Navigation
        data object Save : Navigation
        data object LabelSelect : Navigation
        data object MyPage : Navigation
    }
}
