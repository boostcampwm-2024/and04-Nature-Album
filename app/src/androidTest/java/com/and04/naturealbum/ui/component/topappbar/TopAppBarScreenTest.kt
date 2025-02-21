package com.and04.naturealbum.ui.component.topappbar

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.core.net.toUri
import com.and04.naturealbum.ui.add.savephoto.SavePhotoScreen
import com.and04.naturealbum.ui.album.labelphotos.AlbumFolderScreen
import com.and04.naturealbum.ui.album.labels.AlbumScreen
import com.and04.naturealbum.ui.album.photoinfo.PhotoInfo
import com.and04.naturealbum.ui.home.HomeScreen
import com.and04.naturealbum.ui.mypage.MyPageScreenContent
import com.and04.naturealbum.ui.utils.LocationHandler
import com.and04.naturealbum.ui.utils.UiState
import org.junit.Rule
import org.junit.Test

class TopAppBarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun 홈_화면() {
        composeTestRule.setContent {
            HomeScreen(LocationHandler(LocalContext.current),
                {},
                {},
                {},
                {}
            )
        }

        composeTestRule
            .onNodeWithText("Nature Album")
            .assertExists()


        composeTestRule
            .onNodeWithTag("navigation")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("action")
            .assertExists()
    }

    @Test
    fun 마이페이지_화면() {
        composeTestRule.setContent {
            MyPageScreenContent(
                {},
                {},
                mutableStateOf(UiState.Idle),
                mutableStateOf(emptyList()),
                mutableStateOf(emptyList()),
                { _ -> },
                { _ -> },
                { _ -> },
                mutableStateOf(""),
                mutableStateOf(0),
                { _ -> },
                mutableStateOf(false),
                { _ -> },
                mutableStateOf(false),
                {}
            )
        }

        composeTestRule
            .onNodeWithText("Nature Album")
            .assertExists()

        composeTestRule
            .onNodeWithTag("navigation")
            .assertExists()

        composeTestRule
            .onNodeWithTag("action")
            .assertDoesNotExist()
    }

    @Test
    fun 앨범_등록_화면() {
        composeTestRule.setContent {
            SavePhotoScreen(
                "".toUri(),
                "fileName: String",
                mutableStateOf(null),
                mutableStateOf(""),
                {},
                mutableStateOf(false),
                {},
                mutableStateOf(UiState.Idle),
                {},
                {},
                {},
                { _, _, _, _, _, _, _ -> },
                null
            )
        }

        composeTestRule
            .onNodeWithText("도감 등록")
            .assertExists()

        composeTestRule
            .onNodeWithTag("navigation")
            .assertExists()

        composeTestRule
            .onNodeWithTag("action")
            .assertExists()
    }

    @Test
    fun 앨범_화면() {
        composeTestRule.setContent {
            AlbumScreen(
                mutableStateOf(emptyList()),
                {},
                {},
                {}
            )
        }

        composeTestRule
            .onNodeWithText("Nature Album")
            .assertExists()

        composeTestRule
            .onNodeWithTag("navigation")
            .assertExists()

        composeTestRule
            .onNodeWithTag("action")
            .assertExists()
    }

    @Test
    fun 앨범_라벨_상세_화면() {
        composeTestRule.setContent {
            AlbumFolderScreen(
                mutableStateOf(UiState.Idle),
                { _ -> },
                { _ -> },
                mutableStateOf(false),
                mutableStateOf(false),
                {},
                {},
                {},
                {},
                mutableStateOf(emptySet()),
                {}
            )
        }

        composeTestRule
            .onNodeWithText("Nature Album")
            .assertExists()

        composeTestRule
            .onNodeWithTag("navigation")
            .assertExists()

        composeTestRule
            .onNodeWithTag("action")
            .assertExists()
    }

    @Test
    fun 앨범_사진_상세_화면() {
        composeTestRule.setContent {
            PhotoInfo(
                {},
                {},
                mutableStateOf(UiState.Idle),
                mutableStateOf(""),
                { _ -> }
            )
        }

        composeTestRule
            .onNodeWithText("사진 정보")
            .assertExists()

        composeTestRule
            .onNodeWithTag("navigation")
            .assertExists()

        composeTestRule
            .onNodeWithTag("action")
            .assertExists()
    }

}
