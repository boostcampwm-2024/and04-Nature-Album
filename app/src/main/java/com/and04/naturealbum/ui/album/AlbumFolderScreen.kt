package com.and04.naturealbum.ui.album

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.graphics.Color.parseColor
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.and04.naturealbum.R
import com.and04.naturealbum.data.room.PhotoDetail
import com.and04.naturealbum.ui.component.AlbumLabel
import com.and04.naturealbum.ui.component.MyTopAppBar
import com.and04.naturealbum.ui.component.RotatingImageLoading
import com.and04.naturealbum.ui.home.PermissionDialogState
import com.and04.naturealbum.ui.home.PermissionDialogs
import com.and04.naturealbum.ui.savephoto.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AlbumFolderScreen(
    selectedAlbumLabel: Int = 0,
    onPhotoClick: (Int) -> Unit,
    albumFolderViewModel: AlbumFolderViewModel = hiltViewModel(),
) {
    LaunchedEffect(selectedAlbumLabel) {
        albumFolderViewModel.loadFolderData(selectedAlbumLabel)
    }
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    val setLoading: (Boolean) -> Unit = { b -> loading = b }

    var editMode by remember { mutableStateOf(false) }
    val isEditMode = { editMode }

    val switchEditMode = { b: Boolean -> editMode = b }

    val checkList = remember { mutableStateOf(setOf<PhotoDetail>()) }
    if (!editMode) checkList.value = setOf()

    Scaffold(topBar = { MyTopAppBar() }) { innerPadding ->
        ItemContainer(
            innerPaddingValues = innerPadding,
            onPhotoClick = onPhotoClick,
            switchEditMode = switchEditMode,
            isEditMode = isEditMode,
            checkList = checkList,
            setLoading = setLoading,
            saveImagesWithLoading = {
                saveImagesWithLoading(
                    context = context,
                    checkList.value.toList(),
                    setLoading,
                    switchEditMode
                )
            },
            albumFolderViewModel = albumFolderViewModel,
        )
    }
    if (loading) {
        RotatingImageLoading(
            drawalbeRes = R.drawable.fish_loading_image,
            stringRes = R.string.album_folder_screen_save_text
        )
    }
    BackHandler(enabled = editMode) {
        if (editMode) editMode = false
    }
}

@Composable
private fun ItemContainer(
    innerPaddingValues: PaddingValues,
    onPhotoClick: (Int) -> Unit,
    switchEditMode: (Boolean) -> Unit,
    isEditMode: () -> Boolean,
    checkList: MutableState<Set<PhotoDetail>>,
    setLoading: (Boolean) -> Unit,
    saveImagesWithLoading: () -> Unit,
    albumFolderViewModel: AlbumFolderViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var permissionDialogState by remember { mutableStateOf(PermissionDialogState.None) }
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { permissionAllow ->
            if (permissionAllow) {
                saveImagesWithLoading()
            } else {
                val activity = context as? Activity ?: return@rememberLauncherForActivityResult
                val hasPreviouslyDeniedPermission =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        WRITE_EXTERNAL_STORAGE
                    )
                if (!hasPreviouslyDeniedPermission)
                    permissionDialogState = PermissionDialogState.GoToSettings
            }
        }
    val uiState = albumFolderViewModel.uiState.collectAsState()
    val label = albumFolderViewModel.label.collectAsState()
    val photoDetails = albumFolderViewModel.photoDetails.collectAsState()



    when (uiState.value) {
        is UiState.Loading, UiState.Idle -> {
            setLoading(true)
        }

        is UiState.Success -> {
            setLoading(false)
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPaddingValues)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AlbumLabel(
                        modifier = Modifier
                            .background(
                                color = Color(parseColor("#${label.value.backgroundColor}")),
                                shape = CircleShape
                            )
                            .fillMaxWidth(0.9f),
                        text = label.value.name,
                        backgroundColor = Color(parseColor("#${label.value.backgroundColor}"))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            state = rememberLazyStaggeredGridState(),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalItemSpacing = 16.dp
                        ) {
                            items(photoDetails.value, key = { item -> item.id }) { photoDetail ->
                                Item(
                                    item = photoDetail,
                                    onPhotoClick = onPhotoClick,
                                    switchEditMode = switchEditMode,
                                    isEditMode = isEditMode,
                                    checkList = checkList,
                                )
                            }
                        }
                        if (isEditMode()) {
                            Button(
                                onClick = {
                                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                                        requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
                                    } else {
                                        saveImagesWithLoading()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                Text(stringResource(R.string.album_folder_screen_save_button))
                            }
                        }
                    }
                }
            }
        }
    }

    PermissionDialogs(
        permissionDialogState = permissionDialogState,
        onDismiss = { permissionDialogState = PermissionDialogState.None },
        onGoToSettings = {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(this)
            }
        }
    )
}

@Composable
private fun Item(
    item: PhotoDetail,
    onPhotoClick: (Int) -> Unit,
    switchEditMode: (Boolean) -> Unit,
    isEditMode: () -> Boolean,
    checkList: MutableState<Set<PhotoDetail>>,
) {
    var isSelected by remember { mutableStateOf(false) }
    if (!isEditMode() && isSelected) isSelected = false
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPhotoClick(item.id) }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                isSelected = true
                                switchEditMode(true)
                                checkList.value += item
                            },
                            onTap = {
                                if (isEditMode()) {
                                    isSelected = !isSelected
                                    if (isSelected) {
                                        checkList.value += item
                                    } else {
                                        checkList.value -= item
                                    }
                                } else {
                                    onPhotoClick(item.id)
                                }
                            })
                    }
            ) {
                AsyncImage(
                    model = item.photoUri,
                    contentDescription = stringResource(R.string.album_folder_screen_item_image_description),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(color = Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.album_folder_screen_item_select_icon_description),
                            tint = MaterialTheme.colorScheme.surface,
                        )
                    }
                }
            }
            Text(
                text = item.description,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
