package app.music_g51_claude_code

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.music_g51_claude_code.data.repository.MusicRepository
import app.music_g51_claude_code.ui.components.MiniPlayer
import app.music_g51_claude_code.ui.home.HomeScreen
import app.music_g51_claude_code.ui.me.MeScreen
import app.music_g51_claude_code.ui.player.PlayerScreen
import app.music_g51_claude_code.ui.theme.MusicPlayerTheme
import app.music_g51_claude_code.viewmodel.LibraryViewModel
import app.music_g51_claude_code.viewmodel.PlayerViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Me : Screen("me", "我的", Icons.Default.Person)
    data object Player : Screen("player", "播放", Icons.Default.Home)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val repository = remember { MusicRepository.getInstance(context) }
    val libraryViewModel: LibraryViewModel = viewModel()
    val playerViewModel: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory(repository))

    val libraryState by libraryViewModel.state.collectAsState()
    val playerState by playerViewModel.state.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showPlayer = currentRoute == Screen.Player.route

    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        libraryViewModel.setPermissionGranted(granted)
        if (!granted) {
            Toast.makeText(context, "需要存储权限才能扫描音乐", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            libraryViewModel.setPermissionGranted(true)
            libraryViewModel.refreshLibrary()
        } else {
            val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            permissionLauncher.launch(perm)
        }
    }

    LaunchedEffect(Unit) {
        playerViewModel.connectController(context)
    }

    DisposableEffect(Unit) {
        onDispose { playerViewModel.disconnectController() }
    }

    LaunchedEffect(libraryState.songs) {
        libraryViewModel.loadFavorites()
    }

    Scaffold(
        bottomBar = {
            Column {
                if (playerState.currentSong != null && !showPlayer) {
                    MiniPlayer(
                        song = playerState.currentSong,
                        isPlaying = playerState.isPlaying,
                        onTogglePlayPause = { playerViewModel.togglePlayPause() },
                        onClick = { navController.navigate(Screen.Player.route) }
                    )
                }

                if (!showPlayer) {
                    NavigationBar {
                        val items = listOf(Screen.Home, Screen.Me)
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    songs = libraryState.filteredSongs,
                    searchQuery = libraryState.searchQuery,
                    isLoading = libraryState.isLoading,
                    onSearch = { libraryViewModel.search(it) },
                    onRefresh = { libraryViewModel.refreshLibrary() },
                    onSongClick = { song, list ->
                        playerViewModel.playSong(song, list)
                        navController.navigate(Screen.Player.route)
                    }
                )
            }

            composable(Screen.Me.route) {
                MeScreen(
                    favoriteSongs = libraryState.favoriteSongs,
                    localSongCount = libraryState.songs.size,
                    onSongClick = { song, list ->
                        playerViewModel.playSong(song, list)
                        navController.navigate(Screen.Player.route)
                    }
                )
            }

            composable(Screen.Player.route) {
                PlayerScreen(
                    state = playerState,
                    onBack = { navController.popBackStack() },
                    onPlayPause = { playerViewModel.togglePlayPause() },
                    onNext = { playerViewModel.playNext() },
                    onPrevious = { playerViewModel.playPrevious() },
                    onSeekTo = { playerViewModel.seekTo(it) },
                    onToggleFavorite = { playerViewModel.toggleFavorite() },
                    onToggleLyrics = { playerViewModel.toggleLyrics() }
                )
            }
        }
    }
}
