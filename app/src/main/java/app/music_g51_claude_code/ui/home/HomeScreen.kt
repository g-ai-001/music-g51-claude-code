package app.music_g51_claude_code.ui.home

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.music_g51_claude_code.data.entity.Playlist
import app.music_g51_claude_code.data.entity.Song
import coil.compose.AsyncImage

enum class HomeTab(val label: String) {
    RECOMMEND("推荐"),
    PLAYLISTS("歌单"),
    ARTISTS("歌手"),
    ALBUMS("专辑")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    artists: List<String>,
    albums: List<String>,
    searchQuery: String,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(searchQuery) }
    var selectedTab by remember { mutableStateOf(HomeTab.RECOMMEND) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                if (showSearch) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            onSearch(it)
                        },
                        placeholder = { Text("搜索本地音乐…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    Text("本地音乐", style = MaterialTheme.typography.titleLarge)
                }
            },
            actions = {
                IconButton(onClick = { showSearch = !showSearch; if (!showSearch) { query = ""; onSearch("") } }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            HomeTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) }
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            when (selectedTab) {
                HomeTab.RECOMMEND -> SongList(songs = songs, onSongClick = onSongClick)
                HomeTab.PLAYLISTS -> PlaylistTab(
                    playlists = playlists,
                    onPlaylistClick = onPlaylistClick,
                    onCreatePlaylist = onCreatePlaylist
                )
                HomeTab.ARTISTS -> ArtistList(artists = artists, onArtistClick = onArtistClick)
                HomeTab.ALBUMS -> AlbumList(albums = albums, onAlbumClick = onAlbumClick)
            }
        }
    }
}

@Composable
private fun SongList(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit
) {
    if (songs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到本地音乐", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(songs, key = { it.id }) { song ->
                SongItem(song = song, onClick = { onSongClick(song, songs) })
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = if (song.albumArtUri != null) Uri.parse(song.albumArtUri) else null,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = formatDuration(song.duration),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun PlaylistTab(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCreatePlaylist)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("新建歌单", style = MaterialTheme.typography.bodyLarge)
            }
        }

        items(playlists, key = { it.id }) { playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaylistClick(playlist) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(playlist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ArtistList(
    artists: List<String>,
    onArtistClick: (String) -> Unit
) {
    if (artists.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到歌手", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(artists) { artist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistClick(artist) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(artist, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumList(
    albums: List<String>,
    onAlbumClick: (String) -> Unit
) {
    if (albums.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到专辑", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(albums) { album ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAlbumClick(album) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            Icons.Default.Album,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(album, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
