package com.kread.terminal.view

import android.view.KeyEvent
import android.view.inputmethod.BaseInputConnection

class TerminalInputConnection(private val terminalView: TerminalView) : 
    BaseInputConnection(terminalView, true) {

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        if (text != null) {
            terminalView.sendTextToTerminal(text.toString())
        }
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        if (beforeLength > 0) {
            terminalView.sendTextToTerminal("\b")
        }
        return true
    }

    override fun sendKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            return terminalView.sendKeyEvent(event.keyCode, event)
        }
        return super.sendKeyEvent(event)
    }
}
