package com.kread.terminal

import android.app.Application
import android.content.Context
import java.io.File

class KreadApplication : Application() {

    companion object {
        lateinit var instance: KreadApplication
            private set

        const val KREAD_HOME_PATH = "/data/data/com.kread.terminal/files/home"
        const val KREAD_PREFIX_PATH = "/data/data/com.kread.terminal/files/usr"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupKreadEnvironment()
    }

    private fun setupKreadEnvironment() {
        // Create necessary directories
        val homeDir = File(KREAD_HOME_PATH)
        val prefixDir = File(KREAD_PREFIX_PATH)
        val binDir = File(prefixDir, "bin")
        val tmpDir = File(prefixDir, "tmp")

        homeDir.mkdirs()
        prefixDir.mkdirs()
        binDir.mkdirs()
        tmpDir.mkdirs()

        // Set proper permissions
        homeDir.setReadable(true, false)
        homeDir.setWritable(true, false)
        homeDir.setExecutable(true, false)
    }

    fun getKreadFilesDir(): File {
        return filesDir
    }

    fun getKreadHomeDir(): File {
        return File(KREAD_HOME_PATH)
    }

    fun getKreadPrefixDir(): File {
        return File(KREAD_PREFIX_PATH)
    }
}
