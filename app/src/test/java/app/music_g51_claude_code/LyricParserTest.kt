package app.music_g51_claude_code

import app.music_g51_claude_code.utils.LyricParser
import org.junit.Assert.*
import org.junit.Test

class LyricParserTest {

    @Test
    fun parseLrc_parsesCorrectly() {
        val lrc = """
            [00:00.00]First line
            [00:05.50]Second line
            [01:10.00]Third line
        """.trimIndent()

        val lines = LyricParser.parseLrc(lrc)
        assertEquals(3, lines.size)
        assertEquals(0L, lines[0].timeMs)
        assertEquals("First line", lines[0].text)
        assertEquals(5500L, lines[1].timeMs)
        assertEquals("Second line", lines[1].text)
        assertEquals(70000L, lines[2].timeMs)
        assertEquals("Third line", lines[2].text)
    }

    @Test
    fun parseLrc_skipsEmptyLines() {
        val lrc = "[00:00.00]\n[00:05.00]Hello"
        val lines = LyricParser.parseLrc(lrc)
        assertEquals(1, lines.size)
        assertEquals("Hello", lines[0].text)
    }

    @Test
    fun findCurrentLine_returnsCorrectIndex() {
        val lines = listOf(
            app.music_g51_claude_code.utils.LyricLine(0, "A"),
            app.music_g51_claude_code.utils.LyricLine(5000, "B"),
            app.music_g51_claude_code.utils.LyricLine(10000, "C")
        )

        assertEquals(0, LyricParser.findCurrentLine(lines, 1000))
        assertEquals(1, LyricParser.findCurrentLine(lines, 7000))
        assertEquals(2, LyricParser.findCurrentLine(lines, 15000))
    }

    @Test
    fun findCurrentLine_emptyList() {
        assertEquals(-1, LyricParser.findCurrentLine(emptyList(), 1000))
    }

    @Test
    fun parseLrc_handlesTwoDigitMs() {
        val lrc = "[00:05.12]Test"
        val lines = LyricParser.parseLrc(lrc)
        assertEquals(1, lines.size)
        assertEquals(5120L, lines[0].timeMs)
    }
}
