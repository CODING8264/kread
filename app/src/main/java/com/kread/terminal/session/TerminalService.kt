package com.kread.terminal.session

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.kread.terminal.activity.MainActivity

/**
 * Service to keep terminal sessions alive in background
 */
class TerminalService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "kread_terminal_service"
    }

    private val binder = TerminalBinder()
    private val sessions = mutableListOf<TerminalSession>()

    inner class TerminalBinder : Binder() {
        fun getService(): TerminalService = this@TerminalService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kread Terminal Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps terminal sessions running"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Kread Terminal")
            .setContentText("Terminal session is running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun addSession(session: TerminalSession) {
        sessions.add(session)
    }

    fun removeSession(session: TerminalSession) {
        sessions.remove(session)
        if (sessions.isEmpty()) {
            stopSelf()
        }
    }

    fun getSessions(): List<TerminalSession> = sessions.toList()

    override fun onDestroy() {
        sessions.forEach { it.finish() }
        sessions.clear()
        super.onDestroy()
    }
}
