package app.marlboroadvance.mpvex.ui.browser

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.marlboroadvance.mpvex.presentation.Screen
import app.marlboroadvance.mpvex.ui.browser.folderlist.FolderListScreen
import app.marlboroadvance.mpvex.ui.browser.networkstreaming.NetworkStreamingScreen
import app.marlboroadvance.mpvex.ui.browser.playlist.PlaylistScreen
import app.marlboroadvance.mpvex.ui.browser.recentlyplayed.RecentlyPlayedScreen
import app.marlboroadvance.mpvex.ui.browser.selection.SelectionManager
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
object MainScreen : Screen {

private var persistentSelectedTab: Int = 0

@Volatile
private var isInSelectionModeShared: Boolean = false

@Volatile
private var shouldHideNavigationBar: Boolean = false

@Volatile
private var isBrowserBottomBarVisible: Boolean = false

@Volatile
private var sharedVideoSelectionManager: Any? = null

@Volatile
private var onlyVideosSelected: Boolean = false

@Volatile
private var isPermissionDenied: Boolean = false

fun updateSelectionState(
    isInSelectionMode: Boolean,
    isOnlyVideosSelected: Boolean,
    selectionManager: Any?
) {
    this.isInSelectionModeShared = isInSelectionMode
    this.onlyVideosSelected = isOnlyVideosSelected
    this.sharedVideoSelectionManager = selectionManager
    this.shouldHideNavigationBar = isInSelectionMode && isOnlyVideosSelected
}

fun updatePermissionState(isDenied: Boolean) {
    this.isPermissionDenied = isDenied
}

fun getPermissionDeniedState(): Boolean = isPermissionDenied

fun updateBottomBarVisibility(shouldShow: Boolean) {
    this.shouldHideNavigationBar = !shouldShow
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
override fun Content() {

    var selectedTab by remember { mutableIntStateOf(persistentSelectedTab) }
    val density = LocalDensity.current

    val isInSelectionMode = remember { mutableStateOf(isInSelectionModeShared) }
    val hideNavigationBar = remember { mutableStateOf(shouldHideNavigationBar) }

    val videoSelectionManager =
        remember { mutableStateOf<SelectionManager<*, *>?>(sharedVideoSelectionManager as? SelectionManager<*, *>) }

    LaunchedEffect(Unit) {
        while (true) {

            if (isInSelectionMode.value != isInSelectionModeShared) {
                isInSelectionMode.value = isInSelectionModeShared
            }

            if (hideNavigationBar.value != shouldHideNavigationBar) {
                hideNavigationBar.value = shouldHideNavigationBar
            }

            val currentManager = sharedVideoSelectionManager as? SelectionManager<*, *>
            if (videoSelectionManager.value != currentManager) {
                videoSelectionManager.value = currentManager
            }

            delay(16)
        }
    }

    LaunchedEffect(selectedTab) {
        persistentSelectedTab = selectedTab
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {

            AnimatedVisibility(
                visible = !hideNavigationBar.value,
                enter = slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { it }
                )
            ) {

                NavigationBar(
                    modifier = Modifier.clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                ) {

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, null) },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.History, null) },
                        label = { Text("Recents") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null) },
                        label = { Text("Playlists") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Language, null) },
                        label = { Text("Network") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }
            }
        }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            val fabBottomPadding = 80.dp

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {

                    val slideDistance = with(density) { 48.dp.roundToPx() }
                    val duration = 250

                    if (targetState > initialState) {

                        (slideInHorizontally(
                            animationSpec = tween(duration, easing = FastOutSlowInEasing),
                            initialOffsetX = { slideDistance }
                        ) + fadeIn(
                            animationSpec = tween(duration)
                        )) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(duration),
                                    targetOffsetX = { -slideDistance }
                                ) + fadeOut(
                                    animationSpec = tween(duration / 2)
                                ))

                    } else {

                        (slideInHorizontally(
                            animationSpec = tween(duration),
                            initialOffsetX = { -slideDistance }
                        ) + fadeIn(
                            animationSpec = tween(duration)
                        )) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(duration),
                                    targetOffsetX = { slideDistance }
                                ) + fadeOut(
                                    animationSpec = tween(duration / 2)
                                ))
                    }
                },
                label = "tab_animation"
            ) { tab ->

                CompositionLocalProvider(
                    LocalNavigationBarHeight provides fabBottomPadding
                ) {

                    when (tab) {
                        0 -> FolderListScreen.Content()
                        1 -> RecentlyPlayedScreen.Content()
                        2 -> PlaylistScreen.Content()
                        3 -> NetworkStreamingScreen.Content()
                    }
                }
            }
        }
    }
}

}

val LocalNavigationBarHeight = compositionLocalOf { 0.dp }
