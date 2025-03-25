package com.and04.naturealbum.ui.navigation

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.and04.naturealbum.data.localdata.room.Label
import com.and04.naturealbum.ui.utils.LocationHandler
import com.and04.naturealbum.utils.image.ImageConvert
import java.io.File

@SuppressLint("RestrictedApi")
@Stable
class NatureAlbumState(
    private val context: Context,
) {
    var lastLocation = mutableStateOf<Location?>(null)
    var locationHandler = mutableStateOf(LocationHandler(context))
    var imageUri = mutableStateOf(Uri.EMPTY)
    var selectedLabel = mutableStateOf<Label?>(null)
    private var imageCache = mutableStateOf<File?>(null)
    var currentUid = mutableStateOf("")

    fun handleLauncher(
        result: ActivityResult,
        navigator: NatureAlbumNavigator
    ) {
        if (result.resultCode == RESULT_OK) {
            ImageConvert.resizeImage(imageUri.value) { file, uri ->
                imageCache.value?.delete()
                imageCache.value = file
                imageUri.value = uri
            }

            locationHandler.value.getLocation { location -> lastLocation.value = location }

            navigator.navigateHomeToSaveAlbum()
        } else {
            imageUri.value = Uri.EMPTY
            navigator.navigateCameraToHome()
            selectedLabel.value = null
        }
    }

    fun takePicture(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val fileName = "temp_${System.currentTimeMillis()}"
        imageCache.value = File.createTempFile(fileName, ".jpg", context.cacheDir)
        imageUri.value =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageCache.value!!
            )

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri.value)
            try {
                launcher.launch(this)
            } catch (e: ActivityNotFoundException) {
                // TODO: 카메라 전환 오류 처리
            }
        }
    }

    fun deleteCachePhoto() {
        imageCache.value?.delete()
    }
}

@Composable
fun rememberNatureAlbumState(
    context: Context = LocalContext.current,
): NatureAlbumState {
    val saver = Saver<NatureAlbumState, Map<String, Any?>>(
        save = { state ->
            mapOf(
                "imageUri" to state.imageUri.value.toString(),
                "lastLocation" to state.lastLocation.value,
            )
        },
        restore = { restoredMap ->
            NatureAlbumState(
                context = context,
            ).apply {
                imageUri.value = Uri.parse(restoredMap["imageUri"] as String)
                lastLocation.value = restoredMap["lastLocation"] as Location?
            }
        }
    )

    return rememberSaveable(saver = saver) {
        NatureAlbumState(
            context = context,
        )
    }
}
