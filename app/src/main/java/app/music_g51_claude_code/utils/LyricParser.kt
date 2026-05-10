package app.music_g51_claude_code.utils

import java.io.File

data class LyricLine(val timeMs: Long, val text: String)

object LyricParser {

    fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")

        for (line in lrcContent.lines()) {
            val match = regex.find(line) ?: continue
            val (min, sec, ms, text) = match.destructured
            val timeMs = min.toLong() * 60_000 + sec.toLong() * 1000 +
                    if (ms.length == 3) ms.toLong() else ms.toLong() * 10
            if (text.isNotBlank()) {
                lines.add(LyricLine(timeMs, text.trim()))
            }
        }

        return lines.sortedBy { it.timeMs }
    }

    fun findLrcForSong(songPath: String): String? {
        val audioFile = File(songPath)
        val dir = audioFile.parentFile ?: return null
        val baseName = audioFile.nameWithoutExtension

        val lrcFile = File(dir, "$baseName.lrc")
        if (lrcFile.exists()) return lrcFile.readText()

        return null
    }

    fun findCurrentLine(lines: List<LyricLine>, positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        var idx = lines.lastIndex
        for (i in lines.indices) {
            if (lines[i].timeMs > positionMs) {
                idx = if (i == 0) -1 else i - 1
                break
            }
        }
        return idx
    }
}
