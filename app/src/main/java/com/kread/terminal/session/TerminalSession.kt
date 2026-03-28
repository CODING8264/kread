package com.kread.terminal.session

import android.os.Handler
import android.os.Looper
import android.system.Os
import android.util.Log
import com.kread.terminal.KreadApplication
import com.kread.terminal.emulator.TerminalEmulator
import java.io.*
import java.nio.charset.StandardCharsets

/**
 * Represents a terminal session with a shell process
 */
class TerminalSession(
    private val sessionCallback: SessionCallback
) : TerminalEmulator.TerminalOutput {

    companion object {
        private const val TAG = "TerminalSession"
    }

    interface SessionCallback {
        fun onSessionFinished(session: TerminalSession)
        fun onTextChanged(session: TerminalSession)
        fun onTitleChanged(session: TerminalSession, title: String)
        fun onBell(session: TerminalSession)
    }

    private var terminalEmulator: TerminalEmulator
    private var shellProcess: Process? = null
    private var shellPid: Int = -1
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    var title: String = "kread"
        private set

    init {
        terminalEmulator = TerminalEmulator(80, 24, this)
        startShellProcess()
    }

    private fun startShellProcess() {
        try {
            val shell = "/system/bin/sh"
            val env = buildEnvironment()
            
            val processBuilder = ProcessBuilder(shell)
            processBuilder.environment().putAll(env)
            processBuilder.directory(File(KreadApplication.KREAD_HOME_PATH))
            
            shellProcess = processBuilder.start()
            shellPid = getProcessPid(shellProcess!!)
            
            outputStream = shellProcess!!.outputStream
            inputStream = shellProcess!!.inputStream
            
            // Start reading output
            Thread {
                readProcessOutput()
            }.start()
            
            // Monitor process
            Thread {
                val exitCode = shellProcess!!.waitFor()
                mainHandler.post {
                    sessionCallback.onSessionFinished(this)
                }
            }.start()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start shell process", e)
        }
    }

    private fun buildEnvironment(): Map<String, String> {
        val env = mutableMapOf<String, String>()
        env["TERM"] = "xterm-256color"
        env["HOME"] = KreadApplication.KREAD_HOME_PATH
        env["PREFIX"] = KreadApplication.KREAD_PREFIX_PATH
        env["PATH"] = "${KreadApplication.KREAD_PREFIX_PATH}/bin:/system/bin:/system/xbin"
        env["TMPDIR"] = "${KreadApplication.KREAD_PREFIX_PATH}/tmp"
        env["LANG"] = "en_US.UTF-8"
        env["SHELL"] = "/system/bin/sh"
        env["PWD"] = KreadApplication.KREAD_HOME_PATH
        return env
    }

    private fun getProcessPid(process: Process): Int {
        try {
            val field = process.javaClass.getDeclaredField("pid")
            field.isAccessible = true
            return field.getInt(process)
        } catch (e: Exception) {
            return -1
        }
    }

    private fun readProcessOutput() {
        val buffer = ByteArray(4096)
        try {
            while (true) {
                val read = inputStream?.read(buffer) ?: -1
                if (read == -1) break
                
                if (read > 0) {
                    terminalEmulator.processInput(buffer, 0, read)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading process output", e)
        }
    }

    fun write(data: String) {
        try {
            outputStream?.write(data.toByteArray(StandardCharsets.UTF_8))
            outputStream?.flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error writing to process", e)
        }
    }

    fun write(data: ByteArray) {
        try {
            outputStream?.write(data)
            outputStream?.flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error writing to process", e)
        }
    }

    fun getEmulator(): TerminalEmulator = terminalEmulator

    fun finish() {
        try {
            shellProcess?.destroy()
            outputStream?.close()
            inputStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error finishing session", e)
        }
    }

    override fun onTerminalChanged() {
        mainHandler.post {
            sessionCallback.onTextChanged(this)
        }
    }

    override fun onTitleChanged(title: String) {
        this.title = title
        mainHandler.post {
            sessionCallback.onTitleChanged(this, title)
        }
    }

    override fun onBell() {
        mainHandler.post {
            sessionCallback.onBell(this)
        }
    }
}
