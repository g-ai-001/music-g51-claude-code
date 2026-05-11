package app.music_g51_claude_code.data.dao

import androidx.room.*
import app.music_g51_claude_code.data.entity.Favorite

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: Favorite)

    @Delete
    suspend fun delete(favorite: Favorite)

    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    suspend fun getAllFavoriteIds(): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun count(): Int

    @Query("DELETE FROM favorites WHERE songId NOT IN (:validSongIds)")
    suspend fun deleteOrphans(validSongIds: List<Long>)
}
