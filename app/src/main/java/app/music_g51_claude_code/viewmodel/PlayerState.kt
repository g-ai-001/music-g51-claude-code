package app.music_g51_claude_code.viewmodel

import app.music_g51_claude_code.data.entity.Song
import app.music_g51_claude_code.utils.LyricLine

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0,
    val duration: Long = 0,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isFavorite: Boolean = false,
    val lyrics: List<LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1,
    val showLyrics: Boolean = false,
    val isSeeking: Boolean = false
)
