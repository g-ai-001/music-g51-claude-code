package app.music_g51_claude_code.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.google.common.util.concurrent.ListenableFuture
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.music_g51_claude_code.data.entity.Song
import app.music_g51_claude_code.data.repository.MusicRepository
import app.music_g51_claude_code.service.MusicPlaybackService
import app.music_g51_claude_code.utils.AppLogger
import app.music_g51_claude_code.utils.LyricParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel(
    private val repository: MusicRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var positionJob: Job? = null
    private var lyricJob: Job? = null

    fun connectController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture = future
        future.addListener({
            try {
                val controller = future.get()
                mediaController = controller
                controller.addListener(playerListener)
                startPositionTracking()
            } catch (e: Exception) {
                AppLogger.e("PlayerViewModel", "Failed to connect media controller", e)
            }
        }, context.mainExecutor)
    }

    fun disconnectController() {
        positionJob?.cancel()
        lyricJob?.cancel()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
        val future = mediaControllerFuture
        if (future != null) {
            MediaController.releaseFuture(future)
        }
        mediaControllerFuture = null
    }

    fun playSong(song: Song, playlist: List<Song> = listOf(song)) {
        val controller = mediaController ?: return
        val index = playlist.indexOf(song).coerceAtLeast(0)

        val mediaItems = playlist.map { s ->
            MediaItem.Builder()
                .setUri(s.path)
                .setMediaId(s.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .build()
                )
                .build()
        }

        controller.setMediaItems(mediaItems)
        controller.seekToDefaultPosition(index)
        controller.prepare()
        controller.play()

        loadLyrics(song)

        viewModelScope.launch {
            val isFav = repository.isFavorite(song.id)
            _state.value = _state.value.copy(
                currentSong = song,
                playlist = playlist,
                currentIndex = index,
                isFavorite = isFav
            )
        }

        AppLogger.i("PlayerViewModel", "Playing: ${song.title}")
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    fun playNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) controller.seekToNextMediaItem()
    }

    fun playPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        }
    }

    fun seekTo(position: Long) {
        _state.value = _state.value.copy(isSeeking = false)
        mediaController?.seekTo(position)
    }

    fun onSeeking(position: Long) {
        _state.value = _state.value.copy(position = position, isSeeking = true)
    }

    fun toggleFavorite() {
        val song = _state.value.currentSong ?: return
        viewModelScope.launch {
            if (_state.value.isFavorite) {
                repository.removeFavorite(song.id)
            } else {
                repository.addFavorite(song.id)
            }
            _state.value = _state.value.copy(isFavorite = !_state.value.isFavorite)
        }
    }

    fun toggleLyrics() {
        _state.value = _state.value.copy(showLyrics = !_state.value.showLyrics)
    }

    private fun loadLyrics(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            val lrcContent = LyricParser.findLrcForSong(song.path)
            val lines = if (lrcContent != null) LyricParser.parseLrc(lrcContent) else emptyList()
            _state.value = _state.value.copy(lyrics = lines, currentLyricIndex = -1)
        }
    }

    private fun startPositionTracking() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (isActive) {
                val controller = mediaController ?: break
                if (controller.isPlaying) {
                    val pos = controller.currentPosition
                    val dur = controller.duration.coerceAtLeast(0L)

                    if (!_state.value.isSeeking) {
                        _state.value = _state.value.copy(
                            position = pos,
                            duration = dur,
                            isPlaying = true
                        )
                    } else {
                        _state.value = _state.value.copy(duration = dur, isPlaying = true)
                    }

                    val lyrics = _state.value.lyrics
                    if (lyrics.isNotEmpty() && !_state.value.isSeeking) {
                        val lyricIdx = LyricParser.findCurrentLine(lyrics, pos)
                        if (lyricIdx != _state.value.currentLyricIndex) {
                            _state.value = _state.value.copy(currentLyricIndex = lyricIdx)
                        }
                    }
                } else {
                    _state.value = _state.value.copy(isPlaying = false)
                }
                delay(200)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (mediaItem != null) {
                val idx = mediaController?.currentMediaItemIndex ?: return
                if (idx < _state.value.playlist.size) {
                    val song = _state.value.playlist[idx]
                    _state.value = _state.value.copy(currentSong = song, currentIndex = idx)
                    loadLyrics(song)
                    viewModelScope.launch {
                        val isFav = repository.isFavorite(song.id)
                        _state.value = _state.value.copy(isFavorite = isFav)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectController()
    }

    class Factory(private val repository: MusicRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerViewModel(repository) as T
        }
    }
}
