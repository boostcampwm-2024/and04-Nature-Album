package com.and04.naturealbum.ui.mypage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.and04.naturealbum.background.workmanager.SynchronizationWorker
import com.and04.naturealbum.data.localdata.datastore.DataStoreManager
import com.and04.naturealbum.data.localdata.datastore.DataStoreManager.Companion.NEVER_SYNC
import com.and04.naturealbum.data.model.UserInfo
import com.and04.naturealbum.ui.utils.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager,
    private val userManager: UserManager,
    private val syncDataStore: DataStoreManager,
) : ViewModel() {
    private val _loginState = MutableStateFlow(setInitUiState())
    val loginState: StateFlow<LoginState> = _loginState

    private val _progressState = MutableStateFlow(false)
    val progressState: StateFlow<Boolean> = _progressState

    val recentSyncTime: StateFlow<String> = syncDataStore.syncTime.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NEVER_SYNC
    )

    private val _syncWorking = MutableStateFlow(false)
    val syncWorking: StateFlow<Boolean> = _syncWorking

    fun startSync() {
        viewModelScope.launch {
            var syncWorkingStatus = false
            while (true) {
                val status = SynchronizationWorker.isWorking()
                _syncWorking.value = status

                if (syncWorkingStatus && !status) break
                syncWorkingStatus = status
                delay(1_00L)
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        authenticationManager.signInWithGoogle(context).onEach { response ->
            when (response) {
                is AuthResponse.Success -> {
                    _loginState.emit(
                        getUserInfoUiState()
                    )
                }
            }
            //닫혔을 때
            _progressState.value = false
        }.launchIn(viewModelScope)
        //열렸을 때
        _progressState.value = true
    }

    fun setProgressState(state: Boolean) {
        _progressState.value = state
    }

    private fun getUserInfoUiState(): LoginState {
        val user = UserManager.getUser()
        return LoginState.Login(
            UserInfo(
                userEmail = user?.email,
                userPhotoUri = user?.photoUrl.toString(),
                userDisplayName = user?.displayName,
                userUid = user?.uid
            )
        )
    }

    private fun setInitUiState(): LoginState {
        return if (userManager.isSignIn()) {
            getUserInfoUiState()
        } else {
            LoginState.Logout
        }
    }
}
