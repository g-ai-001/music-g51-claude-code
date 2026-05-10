package app.music_g51_claude_code

import android.app.Application
import app.music_g51_claude_code.utils.AppLogger

class MusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        AppLogger.i("MusicApp", "Application started")
    }
}
