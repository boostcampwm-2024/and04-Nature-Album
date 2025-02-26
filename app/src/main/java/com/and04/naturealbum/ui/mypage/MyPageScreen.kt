package com.and04.naturealbum.ui.mypage

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.and04.naturealbum.R
import com.and04.naturealbum.background.workmanager.SynchronizationWorker
import com.and04.naturealbum.data.dto.FirebaseFriend
import com.and04.naturealbum.data.dto.FirebaseFriendRequest
import com.and04.naturealbum.data.dto.MyFriend
import com.and04.naturealbum.ui.component.AppBarType
import com.and04.naturealbum.ui.component.ProgressIndicator
import com.and04.naturealbum.ui.component.RotatingButton
import com.and04.naturealbum.ui.mypage.friendsearch.FriendViewModel
import com.and04.naturealbum.ui.theme.NatureAlbumTheme
import com.and04.naturealbum.ui.utils.PermissionHandler
import com.and04.naturealbum.utils.GetTopBar
import com.and04.naturealbum.utils.network.NetworkState
import com.and04.naturealbum.utils.network.NetworkState.CONNECTED_DATA
import com.and04.naturealbum.utils.network.NetworkState.CONNECTED_WIFI
import com.and04.naturealbum.utils.network.NetworkState.DISCONNECTED
import com.and04.naturealbum.utils.network.NetworkViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val SOCIAL_LIST_TAB_INDEX = 0
private const val SOCIAL_SEARCH_TAB_INDEX = 1
private const val SOCIAL_ALARM_TAB_INDEX = 2

@Composable
fun MyPageScreen(
    navigateToHome: () -> Unit,
    navigateToFriendSearchScreen: () -> Unit,
    myPageViewModel: MyPageViewModel = hiltViewModel(),
    friendViewModel: FriendViewModel = hiltViewModel(),
    networkViewModel: NetworkViewModel = hiltViewModel(),
) {
    val networkState = networkViewModel.networkState.collectAsStateWithLifecycle()
    val loginState = myPageViewModel.loginState.collectAsStateWithLifecycle()
    val myFriends = friendViewModel.friends.collectAsStateWithLifecycle()
    val receivedFriendRequests =
        friendViewModel.receivedFriendRequests.collectAsStateWithLifecycle()
    val recentSyncTime = myPageViewModel.recentSyncTime.collectAsStateWithLifecycle()
    val progressState = myPageViewModel.progressState.collectAsStateWithLifecycle()
    val syncWorking = myPageViewModel.syncWorking.collectAsStateWithLifecycle()

    MyPageScreenContent(
        navigateToHome = navigateToHome,
        navigateToFriendSearchScreen = navigateToFriendSearchScreen,
        loginState = loginState,
        myFriendsState = myFriends,
        friendRequestsState = receivedFriendRequests,
        signInWithGoogle = myPageViewModel::signInWithGoogle,
        acceptFriendRequest = friendViewModel::acceptFriendRequest,
        rejectFriendRequest = friendViewModel::rejectFriendRequest,
        recentSyncTime = recentSyncTime,
        networkState = networkState,
        initializeFriendViewModel = friendViewModel::initialize,
        progressState = progressState,
        setProgressState = myPageViewModel::setProgressState,
        syncWorking = syncWorking,
        startSync = myPageViewModel::startSync
    )
}

@Composable
fun MyPageScreenContent(
    navigateToHome: () -> Unit,
    navigateToFriendSearchScreen: () -> Unit,
    loginState: State<LoginState>,
    myFriendsState: State<List<FirebaseFriend>>,
    friendRequestsState: State<List<FirebaseFriendRequest>>,
    signInWithGoogle: (Context) -> Unit,
    acceptFriendRequest: (String) -> Unit,
    rejectFriendRequest: (String) -> Unit,
    recentSyncTime: State<String>,
    networkState: State<Int>,
    initializeFriendViewModel: (String) -> Unit,
    progressState: State<Boolean>,
    setProgressState: (Boolean) -> Unit,
    syncWorking: State<Boolean>,
    startSync: () -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            context.GetTopBar(
                type = AppBarType.Navigation,
                navigateToBackScreen = { navigateToHome() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        MyPageContent(
            navigateToFriendSearchScreen = navigateToFriendSearchScreen,
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            loginState = loginState,
            myFriendsState = myFriendsState,
            friendRequestsState = friendRequestsState,
            recentSyncTime = recentSyncTime,
            signInWithGoogle = signInWithGoogle,
            acceptFriendRequest = acceptFriendRequest,
            rejectFriendRequest = rejectFriendRequest,
            snackBarHostState = snackBarHostState,
            networkState = networkState,
            initializeFriendViewModel = initializeFriendViewModel,
            progressState = progressState,
            setProgressState = setProgressState,
            syncWorking = syncWorking,
            startSync = startSync
        )
    }
}

@Composable
private fun MyPageContent(
    navigateToFriendSearchScreen: () -> Unit,
    modifier: Modifier,
    loginState: State<LoginState>,
    myFriendsState: State<List<FirebaseFriend>>,
    friendRequestsState: State<List<FirebaseFriendRequest>>,
    signInWithGoogle: (Context) -> Unit,
    acceptFriendRequest: (String) -> Unit,
    rejectFriendRequest: (String) -> Unit,
    recentSyncTime: State<String>,
    snackBarHostState: SnackbarHostState,
    networkState: State<Int>,
    initializeFriendViewModel: (String) -> Unit,
    progressState: State<Boolean>,
    setProgressState: (Boolean) -> Unit,
    syncWorking: State<Boolean>,
    startSync: () -> Unit,
) {
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {}

    val context = LocalContext.current
    val permissionHandler = remember {
        PermissionHandler(
            context = context,
            allPermissionGranted = {},
            onRequestPermission = { deniedPermissions ->
                requestPermissionLauncher.launch(deniedPermissions)
            },
        )
    }

    Box(modifier = modifier) {
        when (val success = loginState.value) {
            is LoginState.Login -> {
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    val userEmail = success.userInfo.userEmail
                    val userPhotoUri = success.userInfo.userPhotoUri
                    val userDisplayName = success.userInfo.userDisplayName
                    val userUid = success.userInfo.userUid

                    userUid?.let { initializeFriendViewModel(userUid) }

                    SideEffect {
                        permissionHandler.checkPermissions(PermissionHandler.Permissions.NOTIFICATION)
                    }

                    UserProfileContent(
                        uriState = userPhotoUri,
                        emailState = userEmail,
                        displayNameState = userDisplayName,
                        snackBarHostState = snackBarHostState,
                        recentSyncTime = recentSyncTime,
                        networkState = networkState,
                        syncWorking = syncWorking,
                        startSync = startSync
                    )

                    if (networkState.value == DISCONNECTED) {
                        NoNetworkSocialContent()
                    } else {
                        SocialContent(
                            navigateToFriendSearchScreen = navigateToFriendSearchScreen,
                            modifier = Modifier.weight(1f),
                            myFriendsState = myFriendsState,
                            friendRequestsState = friendRequestsState,
                            acceptFriendRequest = acceptFriendRequest,
                            rejectFriendRequest = rejectFriendRequest,
                        )
                    }
                }
            }

            is LoginState.Logout-> {
                Box {
                    ProgressIndicator(progressState.value)
                }
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    UserProfileContent()
                    LoginContent(
                        progressState = progressState,
                        setProgressState = setProgressState,
                    ) { signInWithGoogle(context) }
                }
            }
        }
    }
}

@Composable
fun NoNetworkSocialContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            imageVector = Icons.Default.WifiOff,
            contentDescription = stringResource(R.string.my_page_no_network_social_content_icon_description),
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.my_page_no_network_social_content_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun UserProfileContent(
    uriState: String? = null,
    emailState: String? = null,
    displayNameState: String? = null,
    snackBarHostState: SnackbarHostState? = null,
    recentSyncTime: State<String>? = null,
    networkState: State<Int>? = null,
    syncWorking: State<Boolean>? = null,
    startSync: () -> Unit = {},
) {
    val uri = uriState ?: ""
    val email = emailState ?: stringResource(R.string.my_page_default_user_email)
    val displayName = displayNameState ?: ""

    UserProfileImage(
        uri = uri,
        modifier = Modifier
            .fillMaxHeight(0.2f)
            .aspectRatio(1f)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = displayName,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Text(
            text = email,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (snackBarHostState != null && networkState?.value != DISCONNECTED) {
            SyncContent(
                snackBarHostState = snackBarHostState,
                recentSyncTime = recentSyncTime!!,
                syncWorking = syncWorking!!,
                startSync = startSync
            )
        }
    }
}

@Composable
private fun UserProfileImage(uri: String?, modifier: Modifier) {
    uri?.let {
        AsyncImage(
            model = uri,
            contentDescription = stringResource(R.string.my_page_user_profile_image),
            modifier = modifier.clip(CircleShape)
        )
    } ?: Image(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = stringResource(R.string.my_page_user_profile_image),
        modifier = modifier
    )

}

@Composable
private fun LoginContent(
    progressState: State<Boolean>,
    setProgressState: (Boolean) -> Unit,
    loginHandle: () -> Unit,
) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val context = LocalContext.current

        Text(
            text = stringResource(R.string.my_page_login_txt),
            textAlign = TextAlign.Left
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    if (NetworkState.getNetWorkCode() == NetworkState.DISCONNECTED) {
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.my_page_login_no_network_message),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    } else {
                        loginHandle()
                        setProgressState(true)
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_google_login),
                    contentDescription = null,
                )

                Spacer(modifier = Modifier.width(24.dp))

                Text(text = stringResource(R.string.my_page_google_login_btn))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SocialContent(
    navigateToFriendSearchScreen: () -> Unit,
    modifier: Modifier,
    myFriendsState: State<List<FirebaseFriend>>,
    friendRequestsState: State<List<FirebaseFriendRequest>>,
    acceptFriendRequest: (String) -> Unit,
    rejectFriendRequest: (String) -> Unit,
) {
    val myFriends = myFriendsState.value
    val friendRequests = friendRequestsState.value
    val friendRequestsCount = friendRequests.size

    var tabState by remember { mutableIntStateOf(SOCIAL_LIST_TAB_INDEX) }

    val titles = listOf(
        stringResource(R.string.my_page_social_list),
        stringResource(R.string.my_page_social_search),
        stringResource(R.string.my_page_social_alarm)
    )

    Column(
        modifier = modifier
    ) {
        PrimaryTabRow(selectedTabIndex = tabState) {
            titles.forEachIndexed { index, title ->
                MyPageCustomTab(tabState, index, title, friendRequestsCount) {
                    tabState = index
                }
            }
        }

        when (tabState) {
            SOCIAL_LIST_TAB_INDEX -> MyPageSocialList(myFriends) // 친구 목록
            SOCIAL_SEARCH_TAB_INDEX -> {
                navigateToFriendSearchScreen()
            }

            SOCIAL_ALARM_TAB_INDEX -> MyPageAlarm(
                myAlarms = friendRequests,
                acceptFriendRequest = acceptFriendRequest,
                rejectFriendRequest = rejectFriendRequest,
            )
        }
    }
}

@Composable
private fun MyPageCustomTab(
    tabState: Int,
    index: Int,
    title: String,
    friendRequestsCount: Int,
    onClick: () -> Unit,
) {
    Tab(
        selected = tabState == index,
        onClick = onClick,
        text = {
            Row {
                Text(
                    text = title, maxLines = 2, overflow = TextOverflow.Ellipsis
                )

                Box {
                    if (index == SOCIAL_ALARM_TAB_INDEX && friendRequestsCount > 0) {
                        Badge(
                            modifier = Modifier.padding(start = 8.dp),
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text("$friendRequestsCount")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SyncContent(
    snackBarHostState: SnackbarHostState,
    recentSyncTime: State<String>,
    syncWorking: State<Boolean>,
    startSync: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.my_page_sync))
        IconButton(
            modifier = Modifier.size(24.dp),
            onClick = {
                when (NetworkState.getNetWorkCode()) {
                    CONNECTED_WIFI -> {
                        SynchronizationWorker.runImmediately(context)
                        startSync()
                    }

                    CONNECTED_DATA -> {
                        startSnackBar(
                            context = context,
                            coroutineScope = coroutineScope,
                            snackBarHostState = snackBarHostState,
                            message = context.getString(R.string.my_page_snackbar_network_state_data_keep_going),
                            actionLabel = context.getString(R.string.my_page_snackbar_confirm_button),
                            onClickActionPerformed = startSync
                        )
                    }

                    DISCONNECTED -> {
                        startSnackBar(
                            context = context,
                            coroutineScope = coroutineScope,
                            snackBarHostState = snackBarHostState,
                            message = context.getString(R.string.my_page_snackbar_network_state_disconnect),
                            actionLabel = null
                        )
                    }
                }
            }
        ) {
            RotatingButton(
                rotatingState = syncWorking.value,
                imageVector = Icons.Default.Sync,
                contentDescription = stringResource(R.string.my_page_sync_icon_content_description)
            )
        }
    }
    Text(
        style = MaterialTheme.typography.bodySmall,
        text = recentSyncTime.value
    )

}

private fun startSnackBar(
    context: Context,
    coroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    message: String,
    actionLabel: String?,
    onClickActionPerformed: () -> Unit = {},
) {
    coroutineScope.launch {
        val result = snackBarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long,
        )

        if (result == SnackbarResult.ActionPerformed) {
            SynchronizationWorker.runImmediately(context)
            onClickActionPerformed()
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun MyPageScreenPreview() {
    val loginState = remember { mutableStateOf(LoginState.Logout) }
    val myFriends = remember {
        mutableStateOf(
            listOf(
                MyFriend("", "grand2181@gmail.com", true),
                MyFriend("", "도윤@gmail.com", true),
                MyFriend("", "정호@gmail.com", true)
            )
        )
    }
    val recentSyncTime = remember { mutableStateOf("2024-01-01") }

    // 테스트용 사용자 정보
    val userEmail = remember { mutableStateOf("test@example.com") }
    val userPhotoUrl = remember { mutableStateOf("https://via.placeholder.com/150") }
    val userDisplayName = remember { mutableStateOf("Test User") }

    NatureAlbumTheme {
//        MyPageScreen(
//            navigateToHome = {},
//            loginState = loginState,
//            myFriends = myFriends,
//            userEmail = userEmail.value,
//            userPhotoUrl = userPhotoUrl.value,
//            userDisplayName = userDisplayName.value,
//            signInWithGoogle = {}
//        )
    }
}
