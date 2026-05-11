package app.music_g51_claude_code.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.music_g51_claude_code.data.entity.Playlist
import app.music_g51_claude_code.data.entity.Song
import app.music_g51_claude_code.data.repository.MusicRepository
import app.music_g51_claude_code.utils.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryState(
    val songs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val favoriteSongs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val artists: List<String> = emptyList(),
    val albums: List<String> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository.getInstance(application)
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    fun setPermissionGranted(granted: Boolean) {
        _state.update { it.copy(hasPermission = granted) }
        if (granted) refreshLibrary()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.scanAndSync()
                loadAll()
            } catch (e: Exception) {
                AppLogger.e("LibraryViewModel", "Failed to refresh library", e)
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadAll() {
        val songs = repository.getAllSongs()
        val playlists = repository.getAllPlaylists()
        val artists = repository.getAllArtists()
        val albums = repository.getAllAlbums()
        val query = _state.value.searchQuery
        _state.update { it.copy(
            songs = songs,
            filteredSongs = filterSongs(songs, query),
            playlists = playlists,
            artists = artists,
            albums = albums
        ) }
    }

    private fun filterSongs(songs: List<Song>, query: String): List<Song> {
        if (query.isBlank()) return songs
        return songs.filter { s ->
            s.title.contains(query, true) ||
            s.artist.contains(query, true) ||
            s.album.contains(query, true)
        }
    }

    fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            val results = filterSongs(repository.getAllSongs(), query)
            _state.update { it.copy(filteredSongs = results) }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            val favs = repository.getFavoriteSongs()
            _state.update { it.copy(favoriteSongs = favs) }
        }
    }

    fun getFavoriteCount(): Int = _state.value.favoriteSongs.size

    fun createPlaylist(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createPlaylist(name)
            loadPlaylists()
            onCreated(id)
            AppLogger.i("LibraryViewModel", "Created playlist: $name (id=$id)")
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
            loadPlaylists()
            AppLogger.i("LibraryViewModel", "Deleted playlist: ${playlist.name}")
        }
    }

    fun renamePlaylist(playlist: Playlist, newName: String) {
        viewModelScope.launch {
            repository.updatePlaylist(playlist.copy(name = newName))
            loadPlaylists()
            AppLogger.i("LibraryViewModel", "Renamed playlist to: $newName")
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            AppLogger.i("LibraryViewModel", "Added song $songId to playlist $playlistId")
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            AppLogger.i("LibraryViewModel", "Removed song $songId from playlist $playlistId")
        }
    }

    suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        return repository.getSongsForPlaylist(playlistId)
    }

    suspend fun getSongsByArtist(artist: String): List<Song> {
        return repository.getSongsByArtist(artist)
    }

    suspend fun getSongsByAlbum(album: String): List<Song> {
        return repository.getSongsByAlbum(album)
    }

    private suspend fun loadPlaylists() {
        val playlists = repository.getAllPlaylists()
        _state.update { it.copy(playlists = playlists) }
    }
}
