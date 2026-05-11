package app.music_g51_claude_code.data.repository

import android.content.Context
import app.music_g51_claude_code.data.AppDatabase
import app.music_g51_claude_code.data.entity.Favorite
import app.music_g51_claude_code.data.entity.Playlist
import app.music_g51_claude_code.data.entity.PlaylistSongCrossRef
import app.music_g51_claude_code.data.entity.Song
import app.music_g51_claude_code.utils.AppLogger
import app.music_g51_claude_code.utils.MediaScanner
import kotlinx.coroutines.Dispatchers

class MusicRepository private constructor(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val songDao = db.songDao()
    private val playlistDao = db.playlistDao()
    private val favoriteDao = db.favoriteDao()
    private val appContext = context.applicationContext

    suspend fun scanAndSync() {
        val songs = MediaScanner.scanMusic(appContext)
        songDao.deleteAll()
        songDao.insertAll(songs)
        AppLogger.i("MusicRepository", "Synced ${songs.size} songs to DB")
    }

    suspend fun getAllSongs(): List<Song> = songDao.getAllSongs()
    suspend fun searchSongs(query: String): List<Song> = songDao.searchSongs(query)
    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)
    suspend fun getAllArtists(): List<String> = songDao.getAllArtists()
    suspend fun getSongsByArtist(artist: String): List<Song> = songDao.getSongsByArtist(artist)
    suspend fun getAllAlbums(): List<String> = songDao.getAllAlbums()
    suspend fun getSongsByAlbum(album: String): List<Song> = songDao.getSongsByAlbum(album)

    suspend fun getAllPlaylists(): List<Playlist> = playlistDao.getAllPlaylists()
    suspend fun createPlaylist(name: String): Long = playlistDao.insert(Playlist(name = name))
    suspend fun updatePlaylist(playlist: Playlist) = playlistDao.update(playlist)
    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.delete(playlist)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val count = playlistDao.getSongCountForPlaylist(playlistId)
        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId, songId, count))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        playlistDao.removeSongFromPlaylist(playlistId, songId)

    suspend fun getSongIdsForPlaylist(playlistId: Long): List<Long> =
        playlistDao.getSongIdsForPlaylist(playlistId)

    suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        val ids = playlistDao.getSongIdsForPlaylist(playlistId)
        return if (ids.isEmpty()) emptyList() else songDao.getSongsByIds(ids)
    }

    suspend fun isFavorite(songId: Long): Boolean = favoriteDao.isFavorite(songId)
    suspend fun addFavorite(songId: Long) = favoriteDao.insert(Favorite(songId = songId))
    suspend fun removeFavorite(songId: Long) = favoriteDao.delete(Favorite(songId = songId))
    suspend fun getFavoriteSongIds(): List<Long> = favoriteDao.getAllFavoriteIds()
    suspend fun getFavoriteSongs(): List<Song> {
        val ids = favoriteDao.getAllFavoriteIds()
        return if (ids.isEmpty()) emptyList() else songDao.getSongsByIds(ids)
    }
    suspend fun getFavoriteCount(): Int = favoriteDao.count()

    companion object {
        @Volatile
        private var INSTANCE: MusicRepository? = null

        fun getInstance(context: Context): MusicRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicRepository(context).also { INSTANCE = it }
            }
        }
    }
}
