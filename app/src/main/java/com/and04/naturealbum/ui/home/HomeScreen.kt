package com.and04.naturealbum.ui.home

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.and04.naturealbum.R
import com.and04.naturealbum.ui.component.DialogData
import com.and04.naturealbum.ui.component.MyDialog
import com.and04.naturealbum.ui.component.MyTopAppBar
import com.and04.naturealbum.ui.component.RoundedShapeButton
import com.and04.naturealbum.ui.theme.NatureAlbumTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onTakePicture: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var imageFileName = ""
    val activity = context as? Activity ?: return

    var dialogPermissionGoToSettingsState by remember { mutableStateOf(false) }
    val dialogPermissionExplainState = remember { mutableStateOf(false) }

    val takePictureLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                onTakePicture(imageFileName)
            }
        }

    val allPermissionGranted = {
        imageFileName = "${System.currentTimeMillis()}.jpg"
        val imageFile = File(context.filesDir, imageFileName)
        val imageUri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            try {
                takePictureLauncher.launch(this)
            } catch (e: ActivityNotFoundException) {
                // TODO: 카메라 전환 오류 처리
            }
        }
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val deniedPermissions = permissions.filter { permission -> !permission.value }.keys
            if (deniedPermissions.isEmpty()) {
                allPermissionGranted()
            } else {
                val hasPreviouslyDeniedPermission = deniedPermissions.any { permission ->
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                }
                if (hasPreviouslyDeniedPermission) {
                    dialogPermissionExplainState.value = true
                } else {
                    dialogPermissionGoToSettingsState = true
                }
            }
        }

    val onRequestPermission = { deniedPermissions: Array<String> ->
        requestPermissionLauncher.launch(deniedPermissions)
    }

    val permissionHandler by remember {
        mutableStateOf(
            PermissionHandler(
                context = context,
                activity = activity,
                allPermissionGranted = allPermissionGranted,
                onRequestPermission = onRequestPermission,
                dialogPermissionExplainState = dialogPermissionExplainState
            )
        )
    }

    Scaffold(topBar = { MyTopAppBar() }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            InfoContent(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
            NavigateContent(
                permissionHandler = permissionHandler,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize(),
            )
        }
    }

    if (dialogPermissionExplainState.value) {
        MyDialog(
            DialogData(
                onConfirmation = {
                    dialogPermissionExplainState.value = false
                    permissionHandler.requestPermissions()
                },
                onDismissRequest = { dialogPermissionExplainState.value = false },
                dialogText = R.string.main_activity_permission_explain,
            )
        )
    }

    if (dialogPermissionGoToSettingsState) {
        MyDialog(
            DialogData(
                onConfirmation = {
                    dialogPermissionGoToSettingsState = false
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    context.startActivity(intent)
                },
                onDismissRequest = { dialogPermissionGoToSettingsState = false },
                dialogText = R.string.main_activity_permission_go_to_settings,
            )
        )
    }
}

@Composable
fun InfoContent(modifier: Modifier) {
    RoundedShapeButton("TODO", modifier) { /* TODO */ }
}

@Composable
fun NavigateContent(
    permissionHandler: PermissionHandler,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        RoundedShapeButton("TODO", modifier) { /* TODO */ }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            RoundedShapeButton("TODO", modifier) { /* TODO */ }
            RoundedShapeButton("TODO", modifier) {
                permissionHandler.onClickCamera()
            }
        }
    }

    Spacer(modifier = Modifier.padding(bottom = 72.dp))
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun HomePreview() {
    NatureAlbumTheme {
        HomeScreen()
    }
}