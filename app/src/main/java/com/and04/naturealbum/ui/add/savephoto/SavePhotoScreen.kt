package com.and04.naturealbum.ui.add.savephoto

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.Bitmap
import com.and04.naturealbum.R
import com.and04.naturealbum.background.service.FirebaseInsertService
import com.and04.naturealbum.background.service.FirebaseInsertService.Companion.SERVICE_DATETIME
import com.and04.naturealbum.background.service.FirebaseInsertService.Companion.SERVICE_DESCRIPTION
import com.and04.naturealbum.background.service.FirebaseInsertService.Companion.SERVICE_FILENAME
import com.and04.naturealbum.background.service.FirebaseInsertService.Companion.SERVICE_LABEL
import com.and04.naturealbum.background.service.FirebaseInsertService.Companion.SERVICE_LOCATION_LATITUDE
import com.and04.naturealbum.background.service.FirebaseInsertService.Companion.SERVICE_LOCATION_LONGITUDE
import com.and04.naturealbum.background.service.FirebaseInsertService.Companion.SERVICE_URI
import com.and04.naturealbum.data.localdata.room.Label
import com.and04.naturealbum.ui.add.savephoto.contract.SavePhotoEffect
import com.and04.naturealbum.ui.add.savephoto.contract.SavePhotoIntent
import com.and04.naturealbum.ui.add.savephoto.contract.SavePhotoState
import com.and04.naturealbum.ui.component.AppBarType
import com.and04.naturealbum.ui.component.BackgroundImage
import com.and04.naturealbum.ui.component.ProgressIndicator
import com.and04.naturealbum.ui.component.RotatingImageLoading
import com.and04.naturealbum.ui.theme.NatureAlbumTheme
import com.and04.naturealbum.ui.utils.UiState
import com.and04.naturealbum.ui.utils.UiStatus
import com.and04.naturealbum.utils.GetTopBar
import com.and04.naturealbum.utils.image.ImageConvert
import com.and04.naturealbum.utils.isPortrait
import com.and04.naturealbum.utils.network.NetworkState
import com.and04.naturealbum.utils.network.NetworkState.DISCONNECTED
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.orbitmvi.orbit.compose.collectSideEffect
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SavePhotoScreen(
    state: SavePhotoState,
    initState: SavePhotoState,
    viewModel: SavePhotoViewModel,
) {
    val context = LocalContext.current
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    //val vertexAIState = viewModel.vertexAIState.collectAsStateWithLifecycle()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is SavePhotoEffect.Navigation.Save -> {
                val time = LocalDateTime.now(ZoneId.of("UTC"))
                val fileName = "${System.currentTimeMillis()}.jpg"
                val fileUri = ImageConvert.makeFileToUri(state.uri.toString(), fileName)
                val label = state.appState?.selectedLabel?.value

                viewModel.savePhoto(
                    uri = fileUri,
                    fileName = fileName,
                    label = label!!,
                    location = state.location!!,
                    description = state.description,
                    isRepresented = state.represented,
                    time = time
                )

                insertFirebaseService(
                    context = context,
                    uri = fileUri,
                    fileName = fileName,
                    label = label,
                    location = state.location,
                    description = state.description,
                    time = time
                )
            }

            is SavePhotoEffect.Navigation.Back -> state.onBack()

            is SavePhotoEffect.Navigation.Cancel -> state.onCancel()

            is SavePhotoEffect.Navigation.MyPage -> state.onNavigateToMyPage()

            is SavePhotoEffect.Navigation.LabelSelect -> state.onLabelSelect()
        }
    }

    SavePhotoScreen(
        state = state,
        initState = initState,
        saveState = saveState,
        changeState = viewModel::changeState,
        onIntent = viewModel::onIntent,
    )

//    vertexAI(
//        context = context,
//        uri = initState.uri,
//        vertexAIState = vertexAIState,
//        getGeneratedContent = viewModel::getGeneratedContent
//    )
}

@Composable
fun SavePhotoScreen(
    state: SavePhotoState,
    initState: SavePhotoState,
    saveState: UiState<Unit>,
    changeState: (SavePhotoState) -> Unit,
    onIntent: (SavePhotoIntent) -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            context.GetTopBar(
                title = stringResource(R.string.topbar_title_add_album),
                type = AppBarType.All,
                navigateToMyPage = { onIntent(SavePhotoIntent.MyPageButtonClicked) },
                navigateToBackScreen = { onIntent(SavePhotoIntent.BackButtonClicked) },
            )
        },
    ) { innerPadding ->
        BackgroundImage()

        when (state.status) {
            is UiStatus.Idle -> {
                changeState(
                    if (initState.location == null) initState
                    else initState.copy(status = UiStatus.Success)
                )
            }

            is UiStatus.Loading -> {
                state.getLocation { location ->
                    changeState(state.copy(status = UiStatus.Success, location = location))
                }

                Box(modifier = Modifier.padding(innerPadding)) {
                    ProgressIndicator(state.location == null)
                }
            }

            is UiStatus.Success -> {
                if (context.isPortrait()) {
                    SavePhotoScreenPortrait(
                        innerPadding = innerPadding,
                        state = state,
                        saveState = saveState,
                        onIntent = onIntent,
                    )
                } else {
                    SavePhotoScreenLandscape(
                        innerPadding = innerPadding,
                        state = state,
                        saveState = saveState,
                        onIntent = onIntent,
                    )
                }
            }
        }
    }

    when (saveState) {
        is UiState.Loading -> {
            RotatingImageLoading(
                drawableRes = R.drawable.fish_loading_image,
                stringRes = R.string.save_photo_screen_loading,
            )
        }

        is UiState.Success -> {
            state.onSave()
        }

        else -> Unit
    }

    BackHandler(onBack = { onIntent(SavePhotoIntent.BackButtonClicked) })
}

@Composable
fun IconTextButton(
    enabled: Boolean = true,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    @StringRes stringRes: Int,
    onClick: () -> Unit,
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = imageVector,
                contentDescription = null
            )
            Text(
                text = stringResource(stringRes),
                textAlign = TextAlign.Center,
                modifier = modifier
            )
        }
    }
}

@Composable
fun ToggleButton(
    selected: () -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = { onClick() }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = selected(),
            onClick = { onClick() },
            modifier = modifier
                .size(24.dp)
                .focusable(false),
        )
        Text(stringResource(R.string.save_photo_screen_set_represent))
    }
}

@Composable
fun LabelSelection(
    label: () -> Label?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        Text(
            stringResource(R.string.save_photo_screen_label),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = TextUnit(20f, TextUnitType.Sp),
        )
        Button(
            onClick = { onClick() },
            modifier = modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null
                )
                Box(
                    modifier = modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) {
                    label()?.let { label ->
                        val backgroundColor = Color(label.backgroundColor.toLong(16))
                        SuggestionChip(
                            onClick = { onClick() },
                            label = { Text(text = label.name) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = backgroundColor,
                                labelColor = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White,
                            ),
                            modifier = Modifier.heightIn(max = 24.dp)
                        )
                    } ?: Text(text = stringResource(R.string.save_photo_screen_select_label))
                }
            }
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun Description(
    description: () -> String,
    modifier: Modifier,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = modifier.padding(12.dp)
    ) {
        Text(
            stringResource(R.string.save_photo_screen_description),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = TextUnit(20f, TextUnitType.Sp),
        )
        TextField(
            value = description(),
            onValueChange = { text -> onValueChange(text) },
            placeholder = { Text(stringResource(R.string.save_photo_screen_description_about_photo)) },
            modifier = modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

fun insertFirebaseService(
    context: Context,
    uri: String,
    fileName: String,
    label: Label,
    location: Location,
    description: String,
    time: LocalDateTime,
) {
    if (Firebase.auth.currentUser == null || NetworkState.getNetWorkCode() == DISCONNECTED) return
    val newTime = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val intent = Intent(context, FirebaseInsertService::class.java).apply {
        putExtra(SERVICE_URI, uri)
        putExtra(SERVICE_FILENAME, fileName)
        putExtra(SERVICE_LABEL, label)
        putExtra(SERVICE_LOCATION_LATITUDE, location.latitude)
        putExtra(SERVICE_LOCATION_LONGITUDE, location.longitude)
        putExtra(SERVICE_DESCRIPTION, description)
        putExtra(SERVICE_DATETIME, newTime)
    }

    context.startService(intent)
}

private fun vertexAI(
    context: Context,
    uri: Uri,
    vertexAIState: State<UiState<String>>,
    getGeneratedContent: (Bitmap?) -> Unit
) {
    if (vertexAIState.value is UiState.Idle && NetworkState.getNetWorkCode() != DISCONNECTED) {
        val bitmap = loadImageFromUri(context, uri)
        getGeneratedContent(bitmap)
    }
}

private fun loadImageFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: IOException) {
        null
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun ScreenPreview() {
    NatureAlbumTheme {
        val state = SavePhotoState()

        SavePhotoScreen(
            state = state,
            initState = state,
            saveState = UiState.Idle,
            changeState = {},
            onIntent = {},
        )
    }
}
