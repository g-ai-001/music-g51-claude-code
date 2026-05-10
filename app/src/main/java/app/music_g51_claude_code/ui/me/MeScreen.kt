package app.music_g51_claude_code.ui.me

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.music_g51_claude_code.data.entity.Song
import app.music_g51_claude_code.ui.theme.AccentGreen
import app.music_g51_claude_code.ui.theme.ThemeMode
import coil.compose.AsyncImage

@Composable
fun MeScreen(
    favoriteSongs: List<Song>,
    localSongCount: Int,
    onSongClick: (Song, List<Song>) -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        HeaderCard()

        Spacer(modifier = Modifier.height(16.dp))

        StatsGrid(favoriteCount = favoriteSongs.size, localCount = localSongCount)

        Spacer(modifier = Modifier.height(16.dp))

        ThemeSwitchSection(currentTheme = themeMode, onThemeChange = onThemeChange)

        Spacer(modifier = Modifier.height(24.dp))

        if (favoriteSongs.isNotEmpty()) {
            Text(
                text = "最近收藏",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteSongs.take(10)) { song ->
                    FavoriteCard(song = song, onClick = { onSongClick(song, favoriteSongs) })
                }
            }
        }
    }
}

@Composable
private fun ThemeSwitchSection(currentTheme: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DarkMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("主题模式", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = currentTheme == mode,
                        onClick = { onThemeChange(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    ThemeMode.SYSTEM -> "系统"
                                    ThemeMode.LIGHT -> "浅色"
                                    ThemeMode.DARK -> "深色"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = AccentGreen.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = AccentGreen.copy(alpha = 0.3f)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = AccentGreen
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text("本地用户", style = MaterialTheme.typography.titleMedium)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        "  VIP  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentGreen,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(favoriteCount: Int, localCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(Icons.Default.Favorite, favoriteCount.toString(), "收藏")
        StatItem(Icons.Default.LibraryMusic, localCount.toString(), "本地")
        StatItem(Icons.Default.Headphones, "0", "有声")
        StatItem(Icons.Default.ShoppingBag, "0", "已购")
    }
}

@Composable
private fun StatItem(icon: ImageVector, count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(count, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun FavoriteCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = song.albumArtUri?.let { Uri.parse(it) },
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = song.title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
