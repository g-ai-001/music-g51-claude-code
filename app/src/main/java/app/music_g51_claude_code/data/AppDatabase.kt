package app.music_g51_claude_code.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.music_g51_claude_code.data.dao.FavoriteDao
import app.music_g51_claude_code.data.dao.PlaylistDao
import app.music_g51_claude_code.data.dao.SongDao
import app.music_g51_claude_code.data.entity.Favorite
import app.music_g51_claude_code.data.entity.Playlist
import app.music_g51_claude_code.data.entity.PlaylistSongCrossRef
import app.music_g51_claude_code.data.entity.Song

@Database(
    entities = [Song::class, Playlist::class, PlaylistSongCrossRef::class, Favorite::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
