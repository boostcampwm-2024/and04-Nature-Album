package com.and04.naturealbum.ui.add.savephoto.navigation

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.and04.naturealbum.ui.add.savephoto.SavePhotoScreen
import com.and04.naturealbum.ui.add.savephoto.SavePhotoViewModel
import com.and04.naturealbum.ui.add.savephoto.contract.SavePhotoState.Companion.init
import com.and04.naturealbum.ui.navigation.NatureAlbumNavigator
import com.and04.naturealbum.ui.navigation.NatureAlbumState
import com.and04.naturealbum.ui.navigation.NavigateDestination
import org.orbitmvi.orbit.compose.collectAsState

fun NavGraphBuilder.saveAlbumNavGraph(
    state: NatureAlbumState,
    navigator: NatureAlbumNavigator,
    takePictureLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    composable(NavigateDestination.SavePhoto.route) { backStackEntry ->
        val savePhotoBackStackEntry = remember(backStackEntry) {
            navigator.getNavBackStackEntry(NavigateDestination.SavePhoto.route)
        }
        val viewModel: SavePhotoViewModel =
            hiltViewModel(viewModelStoreOwner = savePhotoBackStackEntry)
        val savePhotoState by viewModel.collectAsState()

        SavePhotoScreen(
            state = { savePhotoState },
            initState = { savePhotoState.init(state, navigator, takePictureLauncher) },
            viewModel = viewModel
        )
    }
}

