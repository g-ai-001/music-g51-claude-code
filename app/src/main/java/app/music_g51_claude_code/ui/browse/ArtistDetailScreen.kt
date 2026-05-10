package app.music_g51_claude_code.ui.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.music_g51_claude_code.data.entity.Song
import app.music_g51_claude_code.ui.home.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artist: String,
    songs: List<Song>,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(artist) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)) {
                Text(artist, style = MaterialTheme.typography.titleLarge)
                Text("${songs.size} 首歌曲", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(songs, key = { it.id }) { song ->
                SongItem(song = song, onClick = { onSongClick(song, songs) })
            }
        }
    }
}
