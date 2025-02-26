package com.and04.naturealbum.ui.mypage

import com.and04.naturealbum.data.model.UserInfo

sealed interface LoginState {

    data class Login(
        val userInfo: UserInfo
    ) : LoginState

    data object Logout : LoginState
}
