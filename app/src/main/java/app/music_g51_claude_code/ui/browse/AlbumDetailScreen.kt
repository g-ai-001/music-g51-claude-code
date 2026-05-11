package app.music_g51_claude_code.ui.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.music_g51_claude_code.data.entity.Song
import app.music_g51_claude_code.ui.components.DetailHeader
import app.music_g51_claude_code.ui.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    album: String,
    songs: List<Song>,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    val albumArtUri = songs.firstOrNull()?.albumArtUri

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(album) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            }
        )

        DetailHeader(
            title = album,
            subtitle = "${songs.size} 首歌曲",
            icon = Icons.Default.Album,
            albumArtUri = albumArtUri
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(songs, key = { it.id }) { song ->
                SongItem(song = song, onClick = { onSongClick(song, songs) })
            }
        }
    }
}
