package com.kread.terminal.emulator

import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * Terminal emulator core - handles VT100/ANSI escape sequences
 */
class TerminalEmulator(
    private val columns: Int,
    private val rows: Int,
    private val terminalOutput: TerminalOutput
) {
    private val screen = TerminalScreen(columns, rows)
    private var cursorRow = 0
    private var cursorCol = 0
    private val escapeSequenceParser = EscapeSequenceParser()

    interface TerminalOutput {
        fun onTerminalChanged()
        fun onTitleChanged(title: String)
        fun onBell()
    }

    fun processInput(data: ByteArray, offset: Int, length: Int) {
        val text = String(data, offset, length, StandardCharsets.UTF_8)
        for (char in text) {
            processChar(char)
        }
        terminalOutput.onTerminalChanged()
    }

    private fun processChar(char: Char) {
        when {
            escapeSequenceParser.isInEscapeSequence() -> {
                escapeSequenceParser.parse(char)
                if (escapeSequenceParser.isComplete()) {
                    handleEscapeSequence(escapeSequenceParser.getSequence())
                    escapeSequenceParser.reset()
                }
            }
            char == '\u001B' -> {
                escapeSequenceParser.start()
            }
            char == '\n' -> {
                cursorRow++
                if (cursorRow >= rows) {
                    screen.scrollUp()
                    cursorRow = rows - 1
                }
            }
            char == '\r' -> {
                cursorCol = 0
            }
            char == '\b' -> {
                if (cursorCol > 0) cursorCol--
            }
            char == '\u0007' -> {
                terminalOutput.onBell()
            }
            char == '\t' -> {
                cursorCol = ((cursorCol / 8) + 1) * 8
                if (cursorCol >= columns) cursorCol = columns - 1
            }
            else -> {
                if (cursorCol >= columns) {
                    cursorCol = 0
                    cursorRow++
                    if (cursorRow >= rows) {
                        screen.scrollUp()
                        cursorRow = rows - 1
                    }
                }
                screen.setChar(cursorRow, cursorCol, char)
                cursorCol++
            }
        }
    }

    private fun handleEscapeSequence(sequence: String) {
        // Handle common ANSI escape sequences
        when {
            sequence.startsWith("[") -> handleCSI(sequence.substring(1))
            sequence.startsWith("]") -> handleOSC(sequence.substring(1))
        }
    }

    private fun handleCSI(params: String) {
        val parts = params.split(Regex("[A-Za-z]"))
        val command = params.lastOrNull { it.isLetter() } ?: return
        val args = if (parts[0].isNotEmpty()) {
            parts[0].split(";").mapNotNull { it.toIntOrNull() }
        } else {
            emptyList()
        }

        when (command) {
            'H', 'f' -> { // Cursor position
                cursorRow = (args.getOrNull(0) ?: 1) - 1
                cursorCol = (args.getOrNull(1) ?: 1) - 1
            }
            'A' -> cursorRow = maxOf(0, cursorRow - (args.getOrNull(0) ?: 1))
            'B' -> cursorRow = minOf(rows - 1, cursorRow + (args.getOrNull(0) ?: 1))
            'C' -> cursorCol = minOf(columns - 1, cursorCol + (args.getOrNull(0) ?: 1))
            'D' -> cursorCol = maxOf(0, cursorCol - (args.getOrNull(0) ?: 1))
            'J' -> { // Clear screen
                when (args.getOrNull(0) ?: 0) {
                    0 -> screen.clearFromCursor(cursorRow, cursorCol)
                    1 -> screen.clearToCursor(cursorRow, cursorCol)
                    2 -> screen.clear()
                }
            }
            'K' -> { // Clear line
                when (args.getOrNull(0) ?: 0) {
                    0 -> screen.clearLineFromCursor(cursorRow, cursorCol)
                    1 -> screen.clearLineToCursor(cursorRow, cursorCol)
                    2 -> screen.clearLine(cursorRow)
                }
            }
        }
    }

    private fun handleOSC(params: String) {
        // Handle Operating System Commands (like setting title)
        if (params.startsWith("0;") || params.startsWith("2;")) {
            val title = params.substring(2).replace("\u0007", "")
            terminalOutput.onTitleChanged(title)
        }
    }

    fun getScreen(): TerminalScreen = screen
    fun getCursorRow(): Int = cursorRow
    fun getCursorCol(): Int = cursorCol
}

class TerminalScreen(private val columns: Int, private val rows: Int) {
    private val buffer = Array(rows) { CharArray(columns) { ' ' } }

    fun setChar(row: Int, col: Int, char: Char) {
        if (row in 0 until rows && col in 0 until columns) {
            buffer[row][col] = char
        }
    }

    fun getChar(row: Int, col: Int): Char {
        return if (row in 0 until rows && col in 0 until columns) {
            buffer[row][col]
        } else ' '
    }

    fun getLine(row: Int): String {
        return if (row in 0 until rows) String(buffer[row]) else ""
    }

    fun scrollUp() {
        for (i in 0 until rows - 1) {
            buffer[i] = buffer[i + 1].copyOf()
        }
        buffer[rows - 1] = CharArray(columns) { ' ' }
    }

    fun clear() {
        for (i in 0 until rows) {
            buffer[i] = CharArray(columns) { ' ' }
        }
    }

    fun clearLine(row: Int) {
        if (row in 0 until rows) {
            buffer[row] = CharArray(columns) { ' ' }
        }
    }

    fun clearFromCursor(row: Int, col: Int) {
        for (r in row until rows) {
            val startCol = if (r == row) col else 0
            for (c in startCol until columns) {
                buffer[r][c] = ' '
            }
        }
    }

    fun clearToCursor(row: Int, col: Int) {
        for (r in 0..row) {
            val endCol = if (r == row) col else columns - 1
            for (c in 0..endCol) {
                buffer[r][c] = ' '
            }
        }
    }

    fun clearLineFromCursor(row: Int, col: Int) {
        if (row in 0 until rows) {
            for (c in col until columns) {
                buffer[row][c] = ' '
            }
        }
    }

    fun clearLineToCursor(row: Int, col: Int) {
        if (row in 0 until rows) {
            for (c in 0..col) {
                buffer[row][c] = ' '
            }
        }
    }
}

class EscapeSequenceParser {
    private var inSequence = false
    private val sequence = StringBuilder()

    fun isInEscapeSequence(): Boolean = inSequence

    fun start() {
        inSequence = true
        sequence.clear()
    }

    fun parse(char: Char) {
        sequence.append(char)
    }

    fun isComplete(): Boolean {
        if (sequence.isEmpty()) return false
        val last = sequence.last()
        return last.isLetter() || last == '~' || last == '\u0007'
    }

    fun getSequence(): String = sequence.toString()

    fun reset() {
        inSequence = false
        sequence.clear()
    }
}
