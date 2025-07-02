package com.and04.naturealbum.ui.add.savephoto.contract

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.and04.naturealbum.ui.navigation.NatureAlbumNavigator
import com.and04.naturealbum.ui.navigation.NatureAlbumState
import com.and04.naturealbum.ui.utils.UiStatus

@Immutable
data class SavePhotoState(
    val status: UiStatus = UiStatus.Idle,
    val appState: NatureAlbumState? = null,
    val uri: Uri? = null,
    val location: Location? = null,
    val description: String = "",
    val represented: Boolean = false,
    val getLocation: @Composable ((Location?) -> Unit) -> Unit = {},
    val onBack: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val onLabelSelect: () -> Unit = {},
    val onNavigateToMyPage: () -> Unit = {}
) {

    companion object {
        fun SavePhotoState.init(
            state: NatureAlbumState,
            navigator: NatureAlbumNavigator,
            takePictureLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
        ): SavePhotoState {
            return copy(
                status = UiStatus.Loading,
                appState = state,
                uri = state.imageUri.value,
                location = state.lastLocation.value,
                getLocation = { doWork ->
                    val locationSettingsLauncher =
                        rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartIntentSenderForResult()
                        ) { result ->
                            if (result.resultCode == RESULT_OK) {
                                state.locationHandler.value.getLocation { location ->
                                    doWork(location)
                                }
                            } else {
                                navigator.navigateToHome()
                            }
                        }

                    state.locationHandler.value.checkLocationSettings(
                        takePicture = {},
                        showGPSActivationDialog = { intentSenderRequest ->
                            locationSettingsLauncher.launch(intentSenderRequest)
                        },
                        airPlaneModeMessage = {}
                    )
                },
                onBack = {
                    state.deleteCachePhoto()
                    state.takePicture(takePictureLauncher)
                },
                onSave = {
                    navigator.navigateSavePhotoToAlbum()
                    state.selectedLabel.value = null
                    state.deleteCachePhoto()
                },
                onCancel = { navigator.navigateToHome() },
                onLabelSelect = { navigator.navigateToSearchLabel() },
                onNavigateToMyPage = { navigator.navigateToMyPage() },
            )
        }
    }
}

