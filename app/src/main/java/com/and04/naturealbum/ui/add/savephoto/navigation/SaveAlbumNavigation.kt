package com.and04.naturealbum.ui.add.savephoto.navigation

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.and04.naturealbum.ui.add.savephoto.SavePhotoScreen
import com.and04.naturealbum.ui.navigation.NatureAlbumNavigator
import com.and04.naturealbum.ui.navigation.NatureAlbumState
import com.and04.naturealbum.ui.navigation.NavigateDestination

fun NavGraphBuilder.saveAlbumNavGraph(
    state: NatureAlbumState,
    navigator: NatureAlbumNavigator,
    takePictureLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    composable(NavigateDestination.SavePhoto.route) { backStackEntry ->
        val savePhotoBackStackEntry = remember(backStackEntry) {
            navigator.getNavBackStackEntry(NavigateDestination.SavePhoto.route)
        }
        SavePhotoScreen(
            locationHandler = state.locationHandler.value,
            location = state.lastLocation.value,
            uri = state.imageUri.value,
            fileName = state.fileName.value,
            onBack = { state.takePicture(takePictureLauncher) },
            onSave = {
                navigator.navigateSavePhotoToAlbum()
                state.selectedLabel.value = null
            },
            onCancel = { navigator.navigateToHome() },
            label = state.selectedLabel.value,
            onLabelSelect = { navigator.navigateToSearchLabel() },
            onNavigateToMyPage = { navigator.navigateToMyPage() },
            viewModel = hiltViewModel(savePhotoBackStackEntry),
        )
    }
}
