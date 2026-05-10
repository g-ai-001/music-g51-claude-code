package app.music_g51_claude_code.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.music_g51_claude_code.data.entity.Song
import app.music_g51_claude_code.data.repository.MusicRepository
import app.music_g51_claude_code.utils.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryState(
    val songs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val favoriteSongs: List<Song> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository.getInstance(application)
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    fun setPermissionGranted(granted: Boolean) {
        _state.value = _state.value.copy(hasPermission = granted)
        if (granted) refreshLibrary()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                repository.scanAndSync()
                loadSongs()
            } catch (e: Exception) {
                AppLogger.e("LibraryViewModel", "Failed to refresh library", e)
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            val songs = repository.getAllSongs()
            _state.value = _state.value.copy(
                songs = songs,
                filteredSongs = if (_state.value.searchQuery.isBlank()) songs
                else songs.filter { s ->
                    s.title.contains(_state.value.searchQuery, true) ||
                    s.artist.contains(_state.value.searchQuery, true) ||
                    s.album.contains(_state.value.searchQuery, true)
                }
            )
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        viewModelScope.launch {
            val results = if (query.isBlank()) repository.getAllSongs()
            else repository.searchSongs(query)
            _state.value = _state.value.copy(filteredSongs = results)
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            val favs = repository.getFavoriteSongs()
            _state.value = _state.value.copy(favoriteSongs = favs)
        }
    }

    fun getFavoriteCount(): Int = _state.value.favoriteSongs.size
}
