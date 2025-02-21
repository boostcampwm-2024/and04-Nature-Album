package com.and04.naturealbum.ui.component.topappbar

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.and04.naturealbum.ui.component.AppBarType
import com.and04.naturealbum.utils.GetTopBar
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TopAppBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val type: MutableState<AppBarType> = mutableStateOf(AppBarType.None)
    private val testTitle: MutableState<String> = mutableStateOf("테스트 타이틀")

    @Before
    fun setup() {
        composeTestRule.setContent {
            val context: Context = LocalContext.current
            context.GetTopBar(
                title = testTitle.value,
                type = type.value,
                navigateToMyPage = { testTitle.value = "Action Click" },
                navigateToBackScreen = { testTitle.value = "Navigation Click" }
            )
        }
    }

    @Test
    fun 모든_버튼_비활성화() {
        type.value = AppBarType.None
        testTitle.value = "테스트 타이틀"

        composeTestRule
            .onNodeWithText("테스트 타이틀")
    }

    @Test
    fun 뒤로가기_버튼만_활성화() {
        type.value = AppBarType.Navigation
        testTitle.value = "테스트 타이틀"

        composeTestRule
            .onNodeWithTag("navigation")
            .assertExists()
            .performClick()

        composeTestRule
            .onNodeWithText("Navigation Click")
            .assertExists()
    }

    @Test
    fun 프로필_버튼만_활성화() {
        type.value = AppBarType.Action
        testTitle.value = "테스트 타이틀"

        composeTestRule
            .onNodeWithTag("action")
            .assertExists()
            .performClick()

        composeTestRule
            .onNodeWithText("Action Click")
            .assertExists()
    }

    @Test
    fun 모든_버튼_활성화() {
        type.value = AppBarType.All
        testTitle.value = "테스트 타이틀"

        composeTestRule
            .onNodeWithTag("navigation")
            .assertExists()
            .performClick()

        composeTestRule
            .onNodeWithText("Navigation Click")
            .assertExists()

        composeTestRule
            .onNodeWithTag("action")
            .assertExists()
            .performClick()

        composeTestRule
            .onNodeWithText("Action Click")
            .assertExists()
    }

}
