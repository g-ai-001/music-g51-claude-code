package app.music_g51_claude_code.utils

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private var logFile: File? = null
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024L
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun init(context: Context) {
        val logDir = context.getExternalFilesDir(null)
        if (logDir != null) {
            if (!logDir.exists()) logDir.mkdirs()
            logFile = File(logDir, "app.log")
            rotateLogIfNeeded()
        }
    }

    private fun rotateLogIfNeeded() {
        val file = logFile ?: return
        if (file.exists() && file.length() > MAX_LOG_SIZE) {
            val old = File(file.parent, "app.log.old")
            if (old.exists()) old.delete()
            file.renameTo(old)
        }
    }

    private fun write(level: String, tag: String, msg: String, throwable: Throwable? = null) {
        val file = logFile ?: return
        try {
            FileWriter(file, true).use { writer ->
                val timestamp = dateFormat.format(Date())
                writer.append("[$timestamp] $level/$tag: $msg\n")
                throwable?.let {
                    val pw = PrintWriter(writer)
                    it.printStackTrace(pw)
                    pw.flush()
                }
            }
        } catch (_: Exception) {}
    }

    fun d(tag: String, msg: String) = write("DEBUG", tag, msg)
    fun i(tag: String, msg: String) = write("INFO", tag, msg)
    fun w(tag: String, msg: String, t: Throwable? = null) = write("WARN", tag, msg, t)
    fun e(tag: String, msg: String, t: Throwable? = null) = write("ERROR", tag, msg, t)
}
