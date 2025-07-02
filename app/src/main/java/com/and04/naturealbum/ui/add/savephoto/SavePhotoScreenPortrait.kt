package com.and04.naturealbum.ui.add.savephoto

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
import com.and04.naturealbum.ui.add.savephoto.contract.SavePhotoIntent
import com.and04.naturealbum.ui.add.savephoto.contract.SavePhotoState
import com.and04.naturealbum.ui.utils.UiState

@Composable
fun SavePhotoScreenPortrait(
    innerPadding: PaddingValues,
    state: () -> SavePhotoState,
    onIntent: (SavePhotoIntent) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(innerPadding)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(state().uri)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.save_photo_screen_image_description),
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(10.dp))
        )

        LabelSelection(
            label = { state().appState?.selectedLabel?.value },
            onClick = state().onLabelSelect,
        )

        Description(
            description = { state().description },
            modifier = Modifier.weight(1f),
            onValueChange = { newDescription ->
                onIntent(
                    SavePhotoIntent.DescriptionInput(
                        newDescription
                    )
                )
            }
        )

        ToggleButton(
            selected = state().represented,
            onClick = { onIntent(SavePhotoIntent.RepresentedToggleClicked) },
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
                onClick = { onIntent(SavePhotoIntent.CancelButtonClicked) })

            IconTextButton(
                enabled = (state().appState?.selectedLabel?.value != null) && (state().saveState != UiState.Loading),
                modifier = Modifier.weight(1f),
                imageVector = Icons.Outlined.Create,
                stringRes = R.string.save_photo_screen_save,
                onClick = { onIntent(SavePhotoIntent.SaveButtonClicked) }
            )
        }
    }
}
