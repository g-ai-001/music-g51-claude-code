package app.music_g51_claude_code.data.dao

import androidx.room.*
import app.music_g51_claude_code.data.entity.Song

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Query("SELECT * FROM songs ORDER BY title COLLATE LOCALIZED ASC")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title COLLATE LOCALIZED ASC")
    suspend fun searchSongs(query: String): List<Song>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIds(ids: List<Long>): List<Song>

    @Query("SELECT DISTINCT artist FROM songs ORDER BY artist COLLATE LOCALIZED ASC")
    suspend fun getAllArtists(): List<String>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY title COLLATE LOCALIZED ASC")
    suspend fun getSongsByArtist(artist: String): List<Song>

    @Query("SELECT DISTINCT album FROM songs ORDER BY album COLLATE LOCALIZED ASC")
    suspend fun getAllAlbums(): List<String>

    @Query("SELECT * FROM songs WHERE album = :album ORDER BY title COLLATE LOCALIZED ASC")
    suspend fun getSongsByAlbum(album: String): List<Song>

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int
}
