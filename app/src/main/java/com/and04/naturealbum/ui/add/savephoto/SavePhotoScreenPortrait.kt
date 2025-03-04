package com.and04.naturealbum.ui.add.savephoto

import android.location.Location
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Create
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.and04.naturealbum.R
import com.and04.naturealbum.data.localdata.room.Label
import com.and04.naturealbum.ui.utils.UiState
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun SavePhotoScreenPortrait(
    innerPadding: PaddingValues,
    uri: Uri,
    fileName: String,
    label: Label?,
    location: Location,
    rememberDescription: State<String>,
    onDescriptionChange: (String) -> Unit,
    isRepresented: State<Boolean>,
    onRepresentedChange: () -> Unit,
    photoSaveState: State<UiState<Unit>>,
    onLabelSelect: () -> Unit,
    onBack: () -> Unit,
    savePhoto: (String, String, Label, Location, String, Boolean, LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(innerPadding)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.save_photo_screen_image_description),
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(10.dp))
        )

        LabelSelection(
            label = label,
            onClick = onLabelSelect,
        )

        Description(description = rememberDescription,
            modifier = Modifier.weight(1f),
            onValueChange = { newDescription -> onDescriptionChange(newDescription) }
        )

        ToggleButton(
            selected = isRepresented,
            onClick = { onRepresentedChange() },
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(start = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTextButton(
                modifier = Modifier.weight(1f),
                imageVector = Icons.Default.Close,
                stringRes = R.string.save_photo_screen_cancel,
                onClick = { onBack() })
            IconTextButton(
                enabled = (label != null) && (photoSaveState.value != UiState.Loading),
                modifier = Modifier.weight(1f),
                imageVector = Icons.Outlined.Create,
                stringRes = R.string.save_photo_screen_save,
                onClick = {
                    val time = LocalDateTime.now(ZoneId.of("UTC"))
                    savePhoto(
                        uri.toString(),
                        fileName,
                        label!!,
                        location,
                        rememberDescription.value,
                        isRepresented.value,
                        time
                    )

                    insertFirebaseService(
                        context = context,
                        uri = uri,
                        fileName = fileName,
                        label = label,
                        location = location,
                        description = rememberDescription.value,
                        time = time
                    )
                })
        }
    }
}
