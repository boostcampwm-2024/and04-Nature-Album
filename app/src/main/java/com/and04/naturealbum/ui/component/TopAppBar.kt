package com.and04.naturealbum.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.and04.naturealbum.R
import com.and04.naturealbum.ui.utils.UserManager

enum class AppBarType {
    None, Navigation, Action, All
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NatureAlbumPortraitTopAppBar(
    title: String,
    type: AppBarType,
    actionTestTag: String = stringResource(R.string.top_bar_action_semantics_test_tag),
    navigationTestTag: String = stringResource(R.string.top_bar_navigation_semantics_test_tag),
    navigateToBackScreen: () -> Unit,
    navigateToMyPage: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (type == AppBarType.All || type == AppBarType.Navigation) {
                IconButton(
                    modifier = Modifier.semantics { testTag = navigationTestTag },
                    onClick = { navigateToBackScreen() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.my_page_arrow_back_icon_content_description)
                    )
                }
            }
        },
        actions = {
            if (type == AppBarType.All || type == AppBarType.Action) {
                MyPageNavigationIconButton(
                    modifier = Modifier.semantics { testTag = actionTestTag },
                    navigateToMyPage = navigateToMyPage
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}

@Composable
fun NatureAlbumLandscapeTopAppBar(
    title: String,
    type: AppBarType,
    actionTestTag: String = stringResource(R.string.top_bar_action_semantics_test_tag),
    navigationTestTag: String = stringResource(R.string.top_bar_navigation_semantics_test_tag),
    navigateToBackScreen: () -> Unit,
    navigateToMyPage: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (type == AppBarType.All || type == AppBarType.Navigation) {
            IconButton(
                onClick = { navigateToBackScreen() },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.top_bar_navigate_to_my_page)
                )
            }
        }

        Text(
            text = title,
            textAlign = TextAlign.Start,
        )

        if (type == AppBarType.All || type == AppBarType.Action) {
            Box {
                MyPageNavigationIconButton(
                    modifier = Modifier.semantics { testTag = actionTestTag },
                    navigateToMyPage = navigateToMyPage
                )
            }
        }
    }
}

@Composable
private fun MyPageNavigationIconButton(
    modifier: Modifier,
    navigateToMyPage: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = { navigateToMyPage() }
    ) {
        UserManager.getUserProfile()?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.top_bar_navigate_to_my_page),
                modifier = Modifier.fillMaxSize()
            )
        } ?: Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(R.string.top_bar_navigate_to_my_page)
        )
    }
}
