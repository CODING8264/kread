package com.kread.terminal.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.kread.terminal.session.TerminalSession

/**
 * Custom view for rendering terminal output
 */
class TerminalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var terminalSession: TerminalSession? = null
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cursorPaint = Paint()
    private var charWidth = 0f
    private var charHeight = 0f
    private var charTop = 0f
    
    companion object {
        private const val TEXT_SIZE = 14f
        private const val TERMINAL_COLUMNS = 80
        private const val TERMINAL_ROWS = 24
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.textSize = TEXT_SIZE * resources.displayMetrics.density
        textPaint.color = 0xFFFFFFFF.toInt()
        
        cursorPaint.color = 0xFF00FF00.toInt()
        
        // Calculate character dimensions
        val fm = textPaint.fontMetrics
        charHeight = Math.ceil((fm.descent - fm.ascent).toDouble()).toFloat()
        charTop = -fm.ascent
        charWidth = textPaint.measureText("M")
        
        setBackgroundColor(0xFF000000.toInt())
    }

    fun attachSession(session: TerminalSession) {
        this.terminalSession = session
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val session = terminalSession ?: return
        val emulator = session.getEmulator()
        val screen = emulator.getScreen()
        
        // Draw terminal text
        for (row in 0 until TERMINAL_ROWS) {
            val line = screen.getLine(row)
            if (line.isNotBlank()) {
                val y = (row + 1) * charHeight
                canvas.drawText(line, 0f, y, textPaint)
            }
        }
        
        // Draw cursor
        val cursorRow = emulator.getCursorRow()
        val cursorCol = emulator.getCursorCol()
        val cursorX = cursorCol * charWidth
        val cursorY = cursorRow * charHeight
        canvas.drawRect(
            cursorX,
            cursorY,
            cursorX + charWidth,
            cursorY + charHeight,
            cursorPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            requestFocus()
            showSoftKeyboard()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onCheckIsTextEditor(): Boolean = true

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = EditorInfo.TYPE_NULL
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN
        return TerminalInputConnection(this)
    }

    fun sendTextToTerminal(text: String) {
        terminalSession?.write(text)
    }

    fun sendKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        val session = terminalSession ?: return false
        
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                session.write("\r")
                return true
            }
            KeyEvent.KEYCODE_DEL -> {
                session.write("\b")
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                session.write("\u001B[A")
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                session.write("\u001B[B")
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                session.write("\u001B[C")
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                session.write("\u001B[D")
                return true
            }
            KeyEvent.KEYCODE_TAB -> {
                session.write("\t")
                return true
            }
        }
        
        return false
    }

    private fun showSoftKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideSoftKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
